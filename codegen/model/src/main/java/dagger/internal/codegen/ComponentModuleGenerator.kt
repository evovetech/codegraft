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

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import dagger.Binds
import dagger.Module
import sourcerer.JavaOutput
import sourcerer.addAnnotation
import sourcerer.addTo
import sourcerer.interfaceBuilder
import sourcerer.toKlass
import sourcerer.typeSpec
import javax.inject.Inject
import javax.lang.model.element.Modifier.ABSTRACT
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.TypeElement

internal
class ComponentModuleGenerator(
    private val descriptor: BootstrapComponentDescriptor,
    private val implGenerator: ComponentImplementationGenerator,
    private val definitionType: TypeElement = descriptor.componentDefinitionType
) : JavaOutput(
    rawType = ClassName.get(definitionType),
    outExt = "Module"
) {
    override
    val include: Boolean = implGenerator.include

    override
    fun newBuilder() = outKlass.interfaceBuilder()

    override
    fun typeSpec() = typeSpec {
        addModifiers(PUBLIC)
        addAnnotation(ClassName.get(Module::class.java).toKlass()) {
            descriptor.applicationModules
                    .map(ModuleDescriptor::moduleElement)
                    .mapNotNull(ClassName::get)
                    .forEach(addTo("includes"))
        }
        if (implGenerator.include) {
            addMethod()
        }
    }

    private
    fun TypeSpec.Builder.addMethod() {
        val returnType = ClassName.get(definitionType)
        val implType = implGenerator.outKlass.rawType
        addMethod(MethodSpec.methodBuilder("bind${definitionType.simpleName}").run {
            addModifiers(PUBLIC, ABSTRACT)
            addAnnotation(Binds::class.java)
            addParameter(implType, implType.simpleName().decapitalize())
            returns(returnType)
            build()
        })
    }

    class Factory
    @Inject constructor() {
        fun create(
            descriptor: BootstrapComponentDescriptor,
            implGenerator: ComponentImplementationGenerator
        ) = ComponentModuleGenerator(
            descriptor = descriptor,
            implGenerator = implGenerator
        )
    }
}
