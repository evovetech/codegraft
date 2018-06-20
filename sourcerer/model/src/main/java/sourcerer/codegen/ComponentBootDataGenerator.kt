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

import com.google.auto.common.MoreTypes
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import sourcerer.Env
import sourcerer.JavaOutput
import sourcerer.classBuilder
import sourcerer.dev.Binding
import sourcerer.dev.ComponentDescriptor
import sourcerer.dev.Dependency
import sourcerer.dev.ModuleDescriptor
import sourcerer.dev.SourcererElements
import sourcerer.dev.SourcererTypes
import sourcerer.inject.BootScope
import sourcerer.typeSpec
import javax.inject.Inject
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PUBLIC

class ComponentBootDataGenerator(
    private val env: Env,
    private val types: SourcererTypes,
    private val elements: SourcererElements,
    private val descriptor: ComponentDescriptor
) : JavaOutput(
    rawType = ClassName.get(descriptor.definitionType),
    outExt = "BootData2"
) {
    override
    fun newBuilder() = outKlass.classBuilder()

    override
    fun typeSpec() = typeSpec {
        addModifiers(PUBLIC, FINAL)
        addAnnotation(BootScope::class.java)
        val bindings = descriptor.modules
                .flatMap(ModuleDescriptor::provisionBindings)
                .filter { it.scope != null }
                .map(Binding::key)
        val dependencies = descriptor.modules
                .flatMap(ModuleDescriptor::dependencies)
                .filter { it.scope != null }
                .map(Dependency::key)
        val constructor = MethodSpec.constructorBuilder()
                .addAnnotation(Inject::class.java)
        val methods = bindings + dependencies
        methods.forEach { key ->
            val element = MoreTypes.asElement(key.type)
            val fieldName = element.simpleName.toString().decapitalize()
            val fieldSpec = addFieldSpec(key.type, fieldName)
            val getterMethodName = "get${fieldName.capitalize()}"
            val getterMethod = MethodSpec.methodBuilder(getterMethodName).run {
                addModifiers(PUBLIC, FINAL)
                // TODO: if any providers
//                addStatement("return \$N.get()", fieldSpec)
                addStatement("return \$N", fieldSpec)
                returns(TypeName.get(key.type))
                build()
            }
            addMethod(getterMethod)
            constructor.addToConstructor(fieldSpec, key.qualifier)
        }
        addMethod(constructor.build())
    }

    class Factory
    @Inject constructor(
        private val env: Env,
        private val types: SourcererTypes,
        private val elements: SourcererElements
    ) {
        fun create(
            descriptor: ComponentDescriptor
        ) = ComponentBootDataGenerator(
            env = env,
            types = types,
            elements = elements,
            descriptor = descriptor
        )
    }
}
