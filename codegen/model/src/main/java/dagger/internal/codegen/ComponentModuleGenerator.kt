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
