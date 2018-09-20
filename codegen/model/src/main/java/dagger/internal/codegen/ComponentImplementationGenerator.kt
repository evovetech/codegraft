/*
 * Copyright (C) 2018 evove.tech
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dagger.internal.codegen

import codegraft.bootstrap.key
import codegraft.bootstrap.qualifier
import codegraft.bootstrap.type
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import dagger.MembersInjector
import dagger.internal.codegen.BootstrapComponentDescriptor.ComponentMethodDescriptor
import dagger.internal.codegen.BootstrapComponentDescriptor.ComponentMethodKind.MEMBERS_INJECTION
import dagger.internal.codegen.BootstrapComponentDescriptor.ComponentMethodKind.PROVISION
import sourcerer.JavaOutput
import sourcerer.addFieldSpec
import sourcerer.addToConstructor
import sourcerer.classBuilder
import sourcerer.getFieldName
import sourcerer.typeSpec
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier.FINAL
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
    val include: Boolean = !descriptor.flatten
                           && descriptor.componentMethods.isNotEmpty()

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
        types: SourcererTypes,
        private val method: ComponentMethodDescriptor
    ) : ExecutableElement by method.methodElement {
        val kind = method.kind
        private val dependency = method.dependencyRequest.get()
        val key = dependency.key
        val type: TypeMirror = key.type
        private val providedType: TypeMirror = when (kind) {
            PROVISION -> {
                type
            }
            MEMBERS_INJECTION -> {
                types.wrapType<MembersInjector<*>>(type)
            }
        }
        private val fieldType = types.wrapType<Provider<*>>(providedType)
        private val fieldName = fieldType.getFieldName()

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
