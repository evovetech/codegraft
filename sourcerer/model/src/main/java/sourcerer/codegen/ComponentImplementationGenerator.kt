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

package sourcerer.codegen

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import sourcerer.Env
import sourcerer.JavaOutput
import sourcerer.classBuilder
import sourcerer.codegen.ComponentImplementationGenerator.Method.Kind.MembersInjector
import sourcerer.codegen.ComponentImplementationGenerator.Method.Kind.Provider
import sourcerer.dev.ComponentDescriptor
import sourcerer.dev.SourcererElements
import sourcerer.dev.SourcererTypes
import sourcerer.dev.abstractMethods
import sourcerer.dev.qualifier
import sourcerer.typeSpec
import javax.inject.Inject
import javax.inject.Singleton
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import kotlin.reflect.KClass

class ComponentImplementationGenerator(
    private val env: Env,
    private val types: SourcererTypes,
    private val elements: SourcererElements,
    private val descriptor: ComponentDescriptor
) : JavaOutput(
    rawType = ClassName.get(descriptor.definitionType),
    outExt = "Implementation2"
) {
    override
    fun newBuilder() = outKlass.classBuilder()

    override
    fun typeSpec() = typeSpec {
        addAnnotation(Singleton::class.java)
        addModifiers(PUBLIC, FINAL)
        addSuperinterface(TypeName.get(descriptor.definitionType.asType()))
        val constructorBuilder = MethodSpec.constructorBuilder()
                .addAnnotation(Inject::class.java)
        val methods = elements.abstractMethods(descriptor.definitionType)
                .map { Method.parse(types, it).apply { write(constructorBuilder) } }
        val constructor = constructorBuilder.build()
                .apply { addMethod(this) }
//        addAnnotation(ClassName.get(ApplicationComponent::class.java).toKlass()) {
//            descriptor.applicationModules
//                    .map(ModuleDescriptor::definitionType)
//                    .mapNotNull(ClassName::get)
//                    .forEach(addTo("modules"))
//        }
    }

    class Method
    private constructor(
        val kind: Kind,
        val types: SourcererTypes,
        method: ExecutableElement
    ) : ExecutableElement by method {
        val element: Element = when (kind) {
            Kind.Provider -> method
            MembersInjector -> method.parameters.first()
        }
        val type: TypeMirror = when (kind) {
            Kind.Provider -> method.returnType
            MembersInjector -> element.asType()
        }
        val name = element.simpleName.toString().let { n ->
            val name = when (kind) {
                Provider -> n.removePrefix("get")
                else -> n
            }
            name.decapitalize()
        }
        val qualifier = element.qualifier
        val fieldName = kind.fieldName(name)
        val fieldType = kind.wrap(types, type)

        private
        fun TypeSpec.Builder.addFieldSpec(): FieldSpec =
            addFieldSpec(fieldType, fieldName)

        private
        fun TypeSpec.Builder.addMethodSpec(fieldSpec: FieldSpec): MethodSpec = MethodSpec.overriding(this@Method)
                .apply {
                    qualifier?.let(AnnotationSpec::get)
                            ?.let(this::addAnnotation)
                    addStatement(kind.methodStatement(fieldSpec, this))
                }
                .build()
                .apply { addMethod(this) }

        fun TypeSpec.Builder.write(constructor: MethodSpec.Builder): Method {
            val fieldSpec = addFieldSpec()
            val methodSpec = addMethodSpec(fieldSpec)
            constructor.addToConstructor(fieldSpec, qualifier)
            return this@Method
        }

        enum class Kind(
            private val wrapperType: KClass<*>
        ) {
            Provider(javax.inject.Provider::class),
            MembersInjector(dagger.MembersInjector::class);

            fun fieldName(name: String) =
                "${name.decapitalize()}${this.name}"

            fun wrap(
                types: SourcererTypes,
                typeMirror: TypeMirror
            ): DeclaredType = types.elements
                    .getTypeElement(wrapperType.java.canonicalName)
                    .let { types.getDeclaredType(it, typeMirror) }

            fun methodStatement(
                fieldSpec: FieldSpec,
                methodSpec: MethodSpec.Builder
            ) = CodeBlock.builder().run {
                when (this@Kind) {
                    Provider -> add("return \$N.get()", fieldSpec)
                    MembersInjector -> {
                        val method = methodSpec.build()
                        add("\$N.injectMembers(\$N)", fieldSpec, method.parameters.first())
                    }
                }
                build()
            }
        }

        companion object Creator {
            @JvmStatic
            fun parse(
                types: SourcererTypes,
                method: ExecutableElement
            ): Method {
                val kind = when (method.parameters.size) {
                    0 -> Kind.Provider
                    else -> MembersInjector
                }
                return Method(kind, types, method)
            }
        }
    }

    class Factory
    @Inject constructor(
        private val env: Env,
        private val types: SourcererTypes,
        private val elements: SourcererElements
    ) {
        fun create(
            descriptor: ComponentDescriptor
        ) = ComponentImplementationGenerator(
            env = env,
            types = types,
            elements = elements,
            descriptor = descriptor
        )
    }
}

fun MethodSpec.Builder.addToConstructor(
    fieldSpec: FieldSpec,
    qualifier: AnnotationMirror? = null
): ParameterSpec = ParameterSpec.builder(fieldSpec.type, fieldSpec.name).run {
    qualifier?.let(AnnotationSpec::get)
            ?.let(this::addAnnotation)
    build()
}.also { paramSpec ->
    addStatement("this.\$N = \$N", fieldSpec, paramSpec)
    addParameter(paramSpec)
}

fun TypeSpec.Builder.addFieldSpec(
    fieldType: TypeMirror,
    fieldName: String
): FieldSpec = addFieldSpec(
    TypeName.get(fieldType),
    fieldName
)

fun TypeSpec.Builder.addFieldSpec(
    fieldType: TypeName,
    fieldName: String
): FieldSpec = FieldSpec.builder(fieldType, fieldName)
        .addModifiers(PRIVATE, FINAL)
        .build()
        .apply { addField(this) }
