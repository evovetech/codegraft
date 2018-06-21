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

package sourcerer.dev

import com.google.auto.common.MoreElements.getAnnotationMirror
import com.google.auto.common.MoreElements.isAnnotationPresent
import com.google.auto.common.MoreTypes
import com.google.common.base.Preconditions.checkArgument
import com.google.common.collect.ImmutableList
import com.google.common.collect.Iterables.getOnlyElement
import com.squareup.javapoet.ClassName
import sourcerer.AnnotatedTypeElement
import sourcerer.inject.BootstrapComponent
import sourcerer.qualifiedName
import java.util.EnumSet
import java.util.Optional
import javax.inject.Inject
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import kotlin.reflect.KClass

data
class ComponentDescriptor(
    val definitionType: TypeElement,
    val annotationMirror: AnnotationMirror,
    val modules: ImmutableList<ModuleDescriptor>,
    val applicationModules: ImmutableList<ModuleDescriptor>
) {
    enum
    class Kind(
        val annotationType: KClass<out Annotation>,
        val builderAnnotationType: KClass<out Annotation>,
        val dependenciesAttribute: String,
        val modulesAttribute: String,
        val isTopLevel: Boolean
    ) {
        Bootstrap(
            BootstrapComponent::class,
            BootstrapComponent.Builder::class,
            BOOTSTRAP_DEPENDENCIES_ATTRIBUTE,
            BOOTSTRAP_MODULES_ATTRIBUTE,
            true
        );

        val AnnotationMirror.modules: ImmutableList<TypeMirror>
            get() = getTypeListValue(modulesAttribute)

        val AnnotationMirror.applicationModules: ImmutableList<TypeMirror>
            get() = when (this@Kind) {
                Bootstrap -> getTypeListValue(APPLICATION_MODULES_ATTRIBUTE)
                else -> ImmutableList.of()
            }

        companion object {
            /**
             * Returns the kind of an annotated element if it is annotated with one of the
             * [annotation types][.annotationType].
             *
             * @throws IllegalArgumentException if the element is annotated with more than one of the
             * annotation types
             */
            @JvmStatic
            fun forAnnotatedElement(element: TypeElement): Kind? {
                val kinds = EnumSet.noneOf(Kind::class.java)
                for (kind in values()) {
                    if (isAnnotationPresent(element, kind.annotationType.java)) {
                        kinds.add(kind)
                    }
                }
                checkArgument(
                    kinds.size <= 1, "%s cannot be annotated with more than one of %s", element, kinds
                )
                return getOnlyElement<Kind>(kinds, null)
            }

            /**
             * Returns the kind of an annotated element if it is annotated with one of the
             * [annotation types][.builderAnnotationType].
             *
             * @throws IllegalArgumentException if the element is annotated with more than one of the
             * annotation types
             */
            @JvmStatic
            fun forAnnotatedBuilderElement(element: TypeElement): Kind? {
                val kinds = EnumSet.noneOf(Kind::class.java)
                for (kind in values()) {
                    if (isAnnotationPresent(element, kind.builderAnnotationType.java)) {
                        kinds.add(kind)
                    }
                }
                checkArgument(
                    kinds.size <= 1, "%s cannot be annotated with more than one of %s", element, kinds
                )
                return getOnlyElement(kinds, null)
            }
        }
    }

    internal
    class Factory
    @Inject constructor(
        val elements: SourcererElements,
        val types: SourcererTypes,
        val moduleFactory: ModuleDescriptor.Factory
    ) {
        fun forStoredComponent(
            className: ClassName
        ): ComponentDescriptor {
            val typeElement = elements.getTypeElement(className.qualifiedName)
            return forComponent(typeElement)
        }

        fun forComponent(
            componentTypeElement: AnnotatedTypeElement<*>
        ): ComponentDescriptor = forComponent(componentTypeElement.element)

        fun forComponent(
            componentDefinitionType: TypeElement
        ): ComponentDescriptor {
            val kind = Kind.forAnnotatedElement(componentDefinitionType)
                       ?: throw IllegalArgumentException("$componentDefinitionType must be annotated with @Component or @ProductionComponent")
            return kind.create(componentDefinitionType)
        }

        private
        fun Kind.create(
            componentDefinitionType: TypeElement,
            parentKind: Kind? = null
        ): ComponentDescriptor {
            val declaredComponentType = MoreTypes.asDeclared(componentDefinitionType.asType())
            val componentMirror = getAnnotationMirror(componentDefinitionType, annotationType.java).get()
            val modules = componentMirror.modules
                    .map(MoreTypes::asTypeElement)
                    .map(moduleFactory::create)
                    .toImmutableList()
            val applicationModules = componentMirror.applicationModules
                    .map(MoreTypes::asTypeElement)
                    .map(moduleFactory::create)
                    .toImmutableList()

            val unimplementedMethods = elements.getUnimplementedMethods(componentDefinitionType)
            unimplementedMethods.map { componentMethod ->
                val resolvedMethod = MoreTypes.asExecutable(types.asMemberOf(declaredComponentType, componentMethod));
            }
            return ComponentDescriptor(
                componentDefinitionType,
                componentMirror,
                modules,
                applicationModules
            )
        }
    }

    /** A function that returns all [.scopes] of its input.  */
    internal data
    class MethodDescriptor(
        val kind: MethodKind,
        val dependencyRequest: Optional<Dependency>,
        val methodElement: ExecutableElement
    ) {
        companion object {
            fun forProvision(
                methodElement: ExecutableElement,
                dependencyRequest: Dependency
            ): MethodDescriptor {
                return MethodDescriptor(
                    MethodKind.PROVISION,
                    Optional.of(dependencyRequest),
                    methodElement
                )
            }

            fun forMembersInjection(
                methodElement: ExecutableElement,
                dependencyRequest: Dependency
            ): MethodDescriptor {
                return MethodDescriptor(
                    MethodKind.MEMBERS_INJECTION,
                    Optional.of(dependencyRequest),
                    methodElement
                )
            }
        }
    }

    internal enum
    class MethodKind {
        PROVISION,
        MEMBERS_INJECTION;
    }
}

