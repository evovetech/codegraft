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

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import dagger.MembersInjector
import dagger.internal.codegen.BootstrapComponentDescriptor2.ComponentMethodDescriptor
import dagger.internal.codegen.BootstrapComponentDescriptor2.ComponentMethodKind.MEMBERS_INJECTION
import dagger.internal.codegen.BootstrapComponentDescriptor2.ComponentMethodKind.PROVISION
import sourcerer.Env
import sourcerer.JavaOutput
import sourcerer.classBuilder
import sourcerer.typeSpec
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.type.TypeMirror

internal
class ComponentImplementationGenerator(
    private val env: Env,
    private val types: SourcererTypes,
    private val elements: SourcererElements,
    private val descriptor: BootstrapComponentDescriptor
) : JavaOutput(
    rawType = ClassName.get(descriptor.definitionType),
    outExt = "Implementation"
) {
    private val descriptor2 = descriptor.descriptor2

    override
    fun newBuilder() = outKlass.classBuilder()

    override
    fun typeSpec() = typeSpec {
        addAnnotation(Singleton::class.java)
        addModifiers(PUBLIC, FINAL)
        addSuperinterface(TypeName.get(descriptor.definitionType.asType()))
        val constructorBuilder = MethodSpec.constructorBuilder()
                .addAnnotation(Inject::class.java)

        val methods = descriptor2?.componentMethods.orEmpty()
                .map { method -> Method(types, method) }
                .map { method -> method.apply { write(constructorBuilder) } }
        val constructor = constructorBuilder.build()
                .apply { addMethod(this) }
    }

    class Method
    internal constructor(
        val types: SourcererTypes,
        val method: ComponentMethodDescriptor
    ) : ExecutableElement by method.methodElement {
        val kind = method.kind
        val dependency = method.dependencyRequest.get()
        val key = dependency.key
        val type: TypeMirror = key.type
        val providedType: TypeMirror
        val fieldName: String = when (kind) {
            PROVISION -> {
                providedType = type
                ""
            }
            MEMBERS_INJECTION -> {
                providedType = types.wrapType<MembersInjector<*>>(type)
                "MembersInjector"
            }
        }.let {
            val elementName = types.asElement(type).simpleName.toString()
            "${elementName.decapitalize()}${it}Provider"
        }
        val fieldType = types.wrapType<Provider<*>>(providedType)

        private
        fun TypeSpec.Builder.addFieldSpec(): FieldSpec =
            addFieldSpec(fieldType, fieldName)

        private
        fun methodStatement(
            fieldSpec: FieldSpec
        ) = CodeBlock.builder().run {
            val provided = CodeBlock.of("\$N.get()", fieldSpec)
            val code = when (kind) {
                PROVISION -> {
                    val returnBlock = CodeBlock.of("return")
                    CodeBlock.join(listOf(returnBlock, provided), " ")
                }
                MEMBERS_INJECTION -> {
                    val paramName = parameters.first().simpleName.toString()
                    val injectMembers = CodeBlock.of("injectMembers(\$L)", paramName)
                    CodeBlock.join(listOf(provided, injectMembers), ".")
                }
            }
            add(code)
            build()
        }

        private
        fun TypeSpec.Builder.addMethodSpec(
            fieldSpec: FieldSpec
        ): MethodSpec = MethodSpec.overriding(this@Method)
                .apply {
                    qualifier?.let(AnnotationSpec::get)
                            ?.let(this::addAnnotation)
                    addStatement(methodStatement(fieldSpec))
                }
                .build()
                .apply { addMethod(this) }

        fun TypeSpec.Builder.write(constructor: MethodSpec.Builder): Method {
            val fieldSpec = addFieldSpec()
            val methodSpec = addMethodSpec(fieldSpec)
            constructor.addToConstructor(fieldSpec, qualifier)
            return this@Method
        }
    }

    class Factory
    @Inject constructor(
        private val env: Env,
        private val types: SourcererTypes,
        private val elements: SourcererElements
    ) {
        fun create(
            descriptor: BootstrapComponentDescriptor
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
