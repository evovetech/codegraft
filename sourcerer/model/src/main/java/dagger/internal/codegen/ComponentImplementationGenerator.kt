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
import dagger.internal.codegen.BootstrapComponentDescriptor.ComponentMethodDescriptor
import dagger.internal.codegen.BootstrapComponentDescriptor.ComponentMethodKind.MEMBERS_INJECTION
import dagger.internal.codegen.BootstrapComponentDescriptor.ComponentMethodKind.PROVISION
import sourcerer.JavaOutput
import sourcerer.bootstrap.getFieldName
import sourcerer.bootstrap.key
import sourcerer.bootstrap.type
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
    private val types: SourcererTypes,
    private val descriptor: BootstrapComponentDescriptor
) : JavaOutput(
    rawType = ClassName.get(descriptor.componentDefinitionType),
    outExt = "Implementation"
) {
    override
    fun newBuilder() = outKlass.classBuilder()

    override
    fun typeSpec() = typeSpec {
        addAnnotation(Singleton::class.java)
        addModifiers(PUBLIC, FINAL)
        addSuperinterface(TypeName.get(descriptor.componentDefinitionType.asType()))
        val constructorBuilder = MethodSpec.constructorBuilder()
                .addAnnotation(Inject::class.java)
        descriptor.componentMethods
                .map { method -> Method(types, method) }
                .map { method -> method.apply { write(constructorBuilder) } }
        addMethod(constructorBuilder.build())
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
        val providedType: TypeMirror = when (kind) {
            PROVISION -> {
                type
            }
            MEMBERS_INJECTION -> {
                types.wrapType<MembersInjector<*>>(type)
            }
        }
        val fieldType = types.wrapType<Provider<*>>(providedType)
        val fieldName = fieldType.getFieldName()

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
        private val types: SourcererTypes
    ) {
        fun create(
            descriptor: BootstrapComponentDescriptor
        ) = ComponentImplementationGenerator(
            types = types,
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
