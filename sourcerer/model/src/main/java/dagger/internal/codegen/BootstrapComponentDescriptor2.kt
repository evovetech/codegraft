/*
 * Copyright 2018 evove.tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dagger.internal.codegen

import com.google.auto.common.MoreElements
import com.google.auto.common.MoreElements.isAnnotationPresent
import com.google.auto.common.MoreTypes
import com.google.auto.value.AutoValue
import com.google.common.base.Preconditions.checkArgument
import com.google.common.base.Verify.verify
import com.google.common.collect.FluentIterable
import com.google.common.collect.ImmutableBiMap
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import com.google.common.collect.Iterables.getOnlyElement
import com.google.common.collect.Sets.immutableEnumSet
import com.google.common.graph.Traverser
import dagger.BindsInstance
import dagger.Component
import dagger.Lazy
import dagger.Module
import dagger.Subcomponent
import dagger.internal.codegen.BootstrapComponentDescriptor2.Kind.BOOTSTRAP_COMPONENT
import dagger.internal.codegen.BootstrapComponentDescriptor2.Kind.SUBCOMPONENT
import dagger.internal.codegen.ConfigurationAnnotations.enclosedBuilders
import dagger.internal.codegen.ConfigurationAnnotations.isSubcomponent
import dagger.internal.codegen.ConfigurationAnnotations.isSubcomponentBuilder
import dagger.internal.codegen.DaggerElements.getAnnotationMirror
import dagger.internal.codegen.InjectionAnnotations.getQualifier
import dagger.internal.codegen.ModuleDescriptor.Kind.MODULE
import dagger.internal.codegen.MoreAnnotationMirrors.getTypeListValue
import dagger.internal.codegen.Scopes.scopesOf
import dagger.model.DependencyRequest
import dagger.model.Scope
import dagger.producers.ProductionComponent
import dagger.releasablereferences.CanReleaseReferences
import sourcerer.inject.BootstrapComponent
import java.util.EnumSet
import java.util.LinkedHashSet
import java.util.Optional
import javax.inject.Inject
import javax.inject.Provider
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ExecutableType
import javax.lang.model.type.TypeKind.DECLARED
import javax.lang.model.type.TypeKind.VOID
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Types
import kotlin.reflect.KClass

/**
 * The logical representation of a [Component] or [ProductionComponent] definition.
 */
@AutoValue
internal abstract
class BootstrapComponentDescriptor2 {
    internal enum
    class Kind(
        val annotationType: KClass<out Annotation>,
        val builderType: KClass<out Annotation>,
        val modulesAttribute: String,
        val isTopLevel: Boolean
    ) {
        BOOTSTRAP_COMPONENT(
            BootstrapComponent::class,
            BootstrapComponent.Builder::class,
            BOOTSTRAP_MODULES_ATTRIBUTE,
            true
        ),
        SUBCOMPONENT(
            Subcomponent::class,
            Subcomponent.Builder::class,
            MODULES_ATTRIBUTE,
            false
        );

        fun annotationType(): Class<out Annotation> {
            return annotationType.java
        }

        fun builderAnnotationType(): Class<out Annotation>? {
            return builderType.java
        }

        fun moduleKinds(): ImmutableSet<ModuleDescriptor.Kind> = when (this) {
            BOOTSTRAP_COMPONENT,
            SUBCOMPONENT -> immutableEnumSet(
                MODULE
            )
        }

        fun subcomponentKinds(): ImmutableSet<Kind> {
            return when (this) {
                BOOTSTRAP_COMPONENT,
                SUBCOMPONENT -> ImmutableSet.of(
                    SUBCOMPONENT
                )
            }
        }

        companion object {

            /**
             * Returns the kind of an annotated element if it is annotated with one of the
             * [annotation types][.annotationType].
             *
             * @throws IllegalArgumentException if the element is annotated with more than one of the
             * annotation types
             */
            fun forAnnotatedElement(element: TypeElement): Optional<Kind> {
                val kinds = EnumSet.noneOf(Kind::class.java)
                for (kind in values()) {
                    if (isAnnotationPresent(element, kind.annotationType())) {
                        kinds.add(kind)
                    }
                }
                checkArgument(
                    kinds.size <= 1, "%s cannot be annotated with more than one of %s", element, kinds
                )
                return Optional.ofNullable(getOnlyElement(kinds, null))
            }

            /**
             * Returns the kind of an annotated element if it is annotated with one of the
             * [annotation types][.builderAnnotationType].
             *
             * @throws IllegalArgumentException if the element is annotated with more than one of the
             * annotation types
             */
            fun forAnnotatedBuilderElement(element: TypeElement): Optional<Kind> {
                val kinds = EnumSet.noneOf(Kind::class.java)
                for (kind in values()) {
                    if (isAnnotationPresent(element, kind.builderAnnotationType()!!)) {
                        kinds.add(kind)
                    }
                }
                checkArgument(
                    kinds.size <= 1, "%s cannot be annotated with more than one of %s", element, kinds
                )
                return Optional.ofNullable(getOnlyElement(kinds, null))
            }
        }
    }

    abstract
    val kind: Kind

    abstract
    val componentAnnotation: AnnotationMirror

    /**
     * The type (interface or abstract class) that defines the component. This is the element to which
     * the [Component] annotation was applied.
     */
    abstract
    val componentDefinitionType: TypeElement

    /**
     * The set of [modules][ModuleDescriptor] declared directly in [Component.modules].
     * Use [.transitiveModules] to get the full set of modules available upon traversing
     * [Module.includes].
     */
    abstract
    val modules: Modules

    abstract
    val applicationModules: Modules?

    /**
     * The scopes of the component.
     */
    abstract
    val scopes: ImmutableSet<Scope>

    /**
     * All [Subcomponent]s which are direct children of this component. This includes
     * subcomponents installed from [Module.subcomponents] as well as subcomponent [ ][.subcomponentsByFactoryMethod] and [ ][.subcomponentsByBuilderMethod].
     */
    val subcomponents: ImmutableSet<BootstrapComponentDescriptor2>
        get() = ImmutableSet.builder<BootstrapComponentDescriptor2>()
                .addAll(subcomponentsByFactoryMethod.values)
                .addAll(subcomponentsByBuilderMethod.values)
                .addAll(subcomponentsFromModules)
                .build()

    /**
     * All [direct child][Subcomponent] components that are declared by a [ ][Module.subcomponents].
     */
    abstract
    val subcomponentsFromModules: ImmutableSet<BootstrapComponentDescriptor2>

    /**
     * All [direct child][Subcomponent] components that are declared by a subcomponent
     * factory method.
     */
    abstract
    val subcomponentsByFactoryMethod: ImmutableBiMap<ComponentMethodDescriptor, BootstrapComponentDescriptor2>

    /**
     * All [direct child][Subcomponent] components that are declared by a subcomponent
     * builder method.
     */
    abstract
    val subcomponentsByBuilderMethod: ImmutableBiMap<ComponentMethodDescriptor, BootstrapComponentDescriptor2>

    /**
     * All [direct child][Subcomponent] components that are declared by an entry point
     * method. This is equivalent to the set of values from [.subcomponentsByFactoryMethod]
     * and [.subcomponentsByBuilderMethod].
     */
    val subcomponentsFromEntryPoints: ImmutableSet<BootstrapComponentDescriptor2>
        get() = ImmutableSet.builder<BootstrapComponentDescriptor2>()
                .addAll(subcomponentsByFactoryMethod.values)
                .addAll(subcomponentsByBuilderMethod.values)
                .build()

    val subcomponentsByBuilderType: ImmutableBiMap<TypeElement, BootstrapComponentDescriptor2> by lazy {
        val subcomponentsByBuilderType = ImmutableBiMap.builder<TypeElement, BootstrapComponentDescriptor2>()
        for (subcomponent in subcomponents) {
            if (subcomponent.builderSpec.isPresent) {
                subcomponentsByBuilderType.put(
                    subcomponent.builderSpec.get().builderDefinitionType(), subcomponent
                )
            }
        }
        subcomponentsByBuilderType.build()
    }

    abstract
    val componentMethods: ImmutableSet<ComponentMethodDescriptor>

    /**
     * The entry point methods on the component type.
     */
    val entryPointMethods: ImmutableSet<ComponentMethodDescriptor>
        get() = componentMethods
                .filter { method -> method.dependencyRequest.isPresent }
                .toImmutableSet()

    // TODO(gak): Consider making this non-optional and revising the
    // interaction between the spec & generation
    internal abstract
    val builderSpec: Optional<BuilderSpec>

    /**
     * For [@Component][Component]s, all [@CanReleaseReferences][CanReleaseReferences]
     * scopes associated with this component or any subcomponent. Otherwise empty.
     */
    val releasableReferencesScopes: ImmutableSet<Scope>
        get() = if (kind == BOOTSTRAP_COMPONENT) {
            FluentIterable.from(SUBCOMPONENT_TRAVERSER.breadthFirst(this))
                    .transformAndConcat { it?.scopes }
                    .filterNotNull()
                    .filter { it.canReleaseReferences() }
                    .toImmutableSet()
        } else {
            ImmutableSet.of()
        }

    /**
     * A function that returns all [.scopes] of its input.
     */
    @AutoValue
    abstract
    class ComponentMethodDescriptor {
        abstract val kind: ComponentMethodKind
        abstract val dependencyRequest: Optional<DependencyRequest>
        abstract val methodElement: ExecutableElement

        companion object {
            fun create(
                kind: ComponentMethodKind,
                dependencyRequest: Optional<DependencyRequest>,
                methodElement: ExecutableElement
            ): ComponentMethodDescriptor = AutoValue_BootstrapComponentDescriptor2_ComponentMethodDescriptor(
                kind,
                dependencyRequest,
                methodElement
            )

            fun forProvision(
                methodElement: ExecutableElement,
                dependencyRequest: DependencyRequest
            ): ComponentMethodDescriptor = create(
                ComponentMethodKind.PROVISION,
                Optional.of(dependencyRequest),
                methodElement
            )

            fun forMembersInjection(
                methodElement: ExecutableElement,
                dependencyRequest: DependencyRequest
            ): ComponentMethodDescriptor = create(
                ComponentMethodKind.MEMBERS_INJECTION,
                Optional.of(dependencyRequest),
                methodElement
            )

            fun forSubcomponent(
                kind: ComponentMethodKind,
                methodElement: ExecutableElement
            ): ComponentMethodDescriptor = create(
                kind,
                Optional.empty(),
                methodElement
            )

            fun forSubcomponentBuilder(
                kind: ComponentMethodKind,
                dependencyRequestForBuilder: DependencyRequest,
                methodElement: ExecutableElement
            ): ComponentMethodDescriptor = create(
                kind,
                Optional.of(dependencyRequestForBuilder),
                methodElement
            )
        }
    }

    internal enum class ComponentMethodKind {
        PROVISION,
        PRODUCTION,
        MEMBERS_INJECTION,
        SUBCOMPONENT,
        SUBCOMPONENT_BUILDER;

        val isSubcomponentKind: Boolean
            get() = this == SUBCOMPONENT

        /**
         * Returns the component kind associated with this component method, if it exists. Otherwise,
         * throws.
         */
        fun componentKind(): Kind = when (this) {
            SUBCOMPONENT,
            SUBCOMPONENT_BUILDER -> Kind.SUBCOMPONENT
            else -> throw IllegalStateException("no component associated with method " + this)
        }
    }

    @AutoValue
    internal abstract class BuilderRequirementMethod {
        internal abstract fun method(): ExecutableElement
        internal abstract fun requirement(): ComponentRequirement
    }

    @AutoValue
    internal abstract class BuilderSpec {
        internal abstract fun builderDefinitionType(): TypeElement
        internal abstract fun requirementMethods(): ImmutableSet<BuilderRequirementMethod>
        internal abstract fun buildMethod(): ExecutableElement
        internal abstract fun componentType(): TypeMirror
    }

    internal class Factory
    @Inject constructor(
        private val elements: DaggerElements,
        private val types: Types,
        private val dependencyRequestFactory: DependencyRequestFactory,
        private val modulesFactory: Modules.Factory
    ) {

        /**
         * Returns a component descriptor for a type annotated with either [@Component][Component]
         * or [@ProductionComponent][ProductionComponent].
         */
        fun forComponent(
            componentDefinitionType: TypeElement
        ): BootstrapComponentDescriptor2 {
            val kind = Kind.forAnnotatedElement(componentDefinitionType)
            checkArgument(
                kind.isPresent && kind.get().isTopLevel,
                "%s must be annotated with @Component or @ProductionComponent",
                componentDefinitionType
            )
            return create(componentDefinitionType, kind.get(), Optional.empty())
        }

        internal
        fun create(
            componentDefinitionType: TypeElement,
            kind: Kind,
            parentKind: Optional<Kind>
        ): BootstrapComponentDescriptor2 {
            val declaredComponentType = MoreTypes.asDeclared(componentDefinitionType.asType())
            val componentMirror = getAnnotationMirror(componentDefinitionType, kind.annotationType()).get()
            val modules = modulesFactory.create(componentMirror, kind.modulesAttribute)
            val applicationModules = when (kind) {
                BOOTSTRAP_COMPONENT -> modulesFactory.create(componentMirror, APPLICATION_MODULES_ATTRIBUTE)
                else -> null
            }
            val subcomponentsFromModules = modules.transitiveModules
                    .flatMap(ModuleDescriptor::subcomponentDeclarations)
                    .map(SubcomponentDeclaration::subcomponentType)
                    .map { subcomponent ->
                        create(
                            subcomponent,
                            Kind.forAnnotatedElement(subcomponent).get(),
                            Optional.of(kind)
                        )
                    }
                    .toImmutableSet()
            val unimplementedMethods = elements.getUnimplementedMethods(componentDefinitionType)
            val componentMethodsBuilder = ImmutableSet.builder<ComponentMethodDescriptor>()

            val subcomponentsByFactoryMethod =
                ImmutableBiMap.builder<ComponentMethodDescriptor, BootstrapComponentDescriptor2>()
            val subcomponentsByBuilderMethod =
                ImmutableBiMap.builder<ComponentMethodDescriptor, BootstrapComponentDescriptor2>()
            for (componentMethod in unimplementedMethods) {
                val resolvedMethod = MoreTypes.asExecutable(types.asMemberOf(declaredComponentType, componentMethod))
                val componentMethodDescriptor =
                    getDescriptorForComponentMethod(componentDefinitionType, kind, componentMethod)
                componentMethodsBuilder.add(componentMethodDescriptor)
                when (componentMethodDescriptor.kind) {
                    BootstrapComponentDescriptor2.ComponentMethodKind.SUBCOMPONENT -> subcomponentsByFactoryMethod.put(
                        componentMethodDescriptor,
                        create(
                            MoreElements.asType(MoreTypes.asElement(resolvedMethod.returnType)),
                            componentMethodDescriptor.kind.componentKind(),
                            Optional.of(kind)
                        )
                    )
                    BootstrapComponentDescriptor2.ComponentMethodKind.SUBCOMPONENT_BUILDER -> subcomponentsByBuilderMethod.put(
                        componentMethodDescriptor,
                        create(
                            MoreElements.asType(MoreTypes.asElement(resolvedMethod.returnType).enclosingElement),
                            componentMethodDescriptor.kind.componentKind(),
                            Optional.of(kind)
                        )
                    )
                    else -> {
                        // nothing special to do for other methods.
                    }
                }
            }

            val enclosedBuilders = if (kind.builderAnnotationType() == null) {
                ImmutableList.of()
            } else {
                enclosedBuilders(componentDefinitionType, kind.builderAnnotationType())
            }
            val builderType = Optional.ofNullable(getOnlyElement(enclosedBuilders, null))
            val builderSpec = createBuilderSpec(builderType)
            val scopes = scopesOf(componentDefinitionType)
            return AutoValue_BootstrapComponentDescriptor2(
                kind,
                componentMirror,
                componentDefinitionType,
                modules,
                applicationModules,
                scopes,
                subcomponentsFromModules,
                subcomponentsByFactoryMethod.build(),
                subcomponentsByBuilderMethod.build(),
                componentMethodsBuilder.build(),
                builderSpec
            )
        }

        private fun getDescriptorForComponentMethod(
            componentElement: TypeElement,
            componentKind: Kind,
            componentMethod: ExecutableElement
        ): ComponentMethodDescriptor {
            val resolvedComponentMethod = MoreTypes.asExecutable(
                types.asMemberOf(MoreTypes.asDeclared(componentElement.asType()), componentMethod)
            )
            val returnType = resolvedComponentMethod.returnType
            if (returnType.kind == DECLARED) {
                if (MoreTypes.isTypeOf(Provider::class.java, returnType)
                    || MoreTypes.isTypeOf(Lazy::class.java, returnType)) {
                    return ComponentMethodDescriptor.forProvision(
                        componentMethod,
                        dependencyRequestFactory.forComponentProvisionMethod(
                            componentMethod,
                            resolvedComponentMethod
                        )
                    )
                } else if (!getQualifier(componentMethod).isPresent) {
                    val returnTypeElement = MoreTypes.asElement(returnType)
                    if (isSubcomponent(returnTypeElement)) {
                        return ComponentMethodDescriptor.forSubcomponent(
                            ComponentMethodKind.SUBCOMPONENT,
                            componentMethod
                        )
                    } else if (isSubcomponentBuilder(returnTypeElement)) {
                        val dependencyRequest = dependencyRequestFactory.forComponentProvisionMethod(
                            componentMethod, resolvedComponentMethod
                        )
                        return ComponentMethodDescriptor.forSubcomponentBuilder(
                            ComponentMethodKind.SUBCOMPONENT_BUILDER,
                            dependencyRequest,
                            componentMethod
                        )
                    }
                }
            }

            // a typical provision method
            if (componentMethod.parameters.isEmpty()
                && componentMethod.returnType.kind != VOID) {
                when (componentKind) {
                    BOOTSTRAP_COMPONENT,
                    SUBCOMPONENT -> return ComponentMethodDescriptor.forProvision(
                        componentMethod,
                        dependencyRequestFactory.forComponentProvisionMethod(
                            componentMethod,
                            resolvedComponentMethod
                        )
                    )
                    else -> throw AssertionError()
                }
            }

            val parameterTypes = resolvedComponentMethod.parameterTypes
            if (parameterTypes.size == 1 && (returnType.kind == VOID || MoreTypes.equivalence().equivalent(
                    returnType,
                    parameterTypes[0]
                ))) {
                return ComponentMethodDescriptor.forMembersInjection(
                    componentMethod,
                    dependencyRequestFactory.forComponentMembersInjectionMethod(
                        componentMethod,
                        resolvedComponentMethod
                    )
                )
            }

            throw IllegalArgumentException("not a valid component method: $componentMethod")
        }

        private fun createBuilderSpec(builderType: Optional<DeclaredType>): Optional<BuilderSpec> {
            if (!builderType.isPresent) {
                return Optional.empty()
            }
            val element = MoreTypes.asTypeElement(builderType.get())
            val methods = elements.getUnimplementedMethods(element)
            val requirementMethods = ImmutableSet.builder<BuilderRequirementMethod>()
            var buildMethod: ExecutableElement? = null
            for (method in methods) {
                if (method.parameters.isEmpty()) {
                    buildMethod = method
                } else {
                    val resolved = MoreTypes.asExecutable(types.asMemberOf(builderType.get(), method))
                    requirementMethods.add(
                        AutoValue_BootstrapComponentDescriptor2_BuilderRequirementMethod(
                            method,
                            requirementForBuilderMethod(method, resolved)
                        )
                    )
                }
            }
            verify(buildMethod != null) // validation should have ensured this.
            return Optional.of(
                AutoValue_BootstrapComponentDescriptor2_BuilderSpec(
                    element,
                    requirementMethods.build(),
                    buildMethod!!,
                    element.enclosingElement.asType()
                )
            )
        }

        private
        fun requirementForBuilderMethod(
            method: ExecutableElement,
            resolvedType: ExecutableType
        ): ComponentRequirement {
            checkArgument(method.parameters.size == 1)
            if (isAnnotationPresent(method, BindsInstance::class.java)) {
                val request = dependencyRequestFactory.forRequiredResolvedVariable(
                    getOnlyElement<VariableElement>(method.parameters),
                    getOnlyElement(resolvedType.parameterTypes)
                )
                return ComponentRequirement.forBoundInstance(
                    request.key(),
                    request.isNullable,
                    method.simpleName.toString()
                )
            }

            val type = getOnlyElement(resolvedType.parameterTypes)
            return if (ConfigurationAnnotations.getModuleAnnotation(MoreTypes.asTypeElement(type)).isPresent) {
                ComponentRequirement.forModule(type)
            } else {
                ComponentRequirement.forDependency(type)
            }
        }
    }

    data
    class Modules(
        val modules: ImmutableSet<ModuleDescriptor>
    ) : Set<ModuleDescriptor> by modules {
        val transitiveModules = transitiveModules(modules)
        val transitiveModuleTypes: ImmutableSet<TypeElement>
            get() = FluentIterable.from(transitiveModules)
                    .transform { it?.moduleElement() }
                    .toSet()

        class Factory
        @Inject constructor(
            val moduleDescriptorFactory: ModuleDescriptor.Factory
        ) {
            fun create(
                componentMirror: AnnotationMirror,
                modulesAttribute: String
            ) = Modules(
                getTypeListValue(componentMirror, modulesAttribute)
                        .map(MoreTypes::asTypeElement)
                        .map(moduleDescriptorFactory::create)
                        .toImmutableSet()
            )
        }
    }

    companion object {
        private fun transitiveModules(
            topLevelModules: Iterable<ModuleDescriptor>
        ): ImmutableSet<ModuleDescriptor> {
            val transitiveModules = LinkedHashSet<ModuleDescriptor>()
            for (module in topLevelModules) {
                addTransitiveModules(transitiveModules, module)
            }
            return ImmutableSet.copyOf(transitiveModules)
        }

        private fun addTransitiveModules(
            transitiveModules: MutableSet<ModuleDescriptor>,
            module: ModuleDescriptor
        ) {
            if (transitiveModules.add(module)) {
                for (includedModule in module.includedModules()) {
                    addTransitiveModules(transitiveModules, includedModule)
                }
            }
        }

        /**
         * [Traverser] for the subcomponent tree.
         */
        private val SUBCOMPONENT_TRAVERSER = Traverser.forTree<BootstrapComponentDescriptor2>(
            BootstrapComponentDescriptor2::subcomponents
        )

        /**
         * No-argument methods defined on [Object] that are ignored for contribution.
         */
        private val NON_CONTRIBUTING_OBJECT_METHOD_NAMES =
            ImmutableSet.of("toString", "hashCode", "clone", "getClass")

        fun isComponentContributionMethod(elements: DaggerElements, method: ExecutableElement): Boolean {
            return (method.parameters.isEmpty()
                    && method.returnType.kind != VOID
                    && elements.getTypeElement(Any::class.java) != method.enclosingElement
                    && !NON_CONTRIBUTING_OBJECT_METHOD_NAMES.contains(method.simpleName.toString()))
        }
    }
}
