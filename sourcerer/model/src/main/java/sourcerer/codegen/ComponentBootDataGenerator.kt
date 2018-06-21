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

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import sourcerer.Env
import sourcerer.JavaOutput
import sourcerer.classBuilder
import sourcerer.dev.ComponentDescriptor
import sourcerer.dev.Key
import sourcerer.dev.Keyed
import sourcerer.dev.ModuleDescriptor
import sourcerer.dev.SourcererElements
import sourcerer.dev.SourcererTypes
import sourcerer.dev.fieldName
import sourcerer.dev.getterMethod
import sourcerer.inject.BootScope
import sourcerer.typeSpec
import javax.inject.Inject
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PUBLIC

val Collection<Keyed>.keys: Set<Key>
    get() = map(Keyed::key)
            .toSet()

class ComponentBootDataGenerator(
    private val env: Env,
    private val types: SourcererTypes,
    private val elements: SourcererElements,
    private val descriptor: ComponentDescriptor
) : JavaOutput(
    rawType = ClassName.get(descriptor.definitionType),
    outExt = "BootData2"
) {
    private val allBindings by lazy {
        descriptor.modules.flatMap(ModuleDescriptor::provisionBindings)
    }
    private val scopedBindings by lazy {
        allBindings.filter { it.scope != null }
    }
    private val allDependencies by lazy {
        descriptor.modules.flatMap(ModuleDescriptor::dependencies)
    }
    private val scopedDependencies by lazy {
        allDependencies.filter { it.scope != null }
    }

    val scopedKeys: Set<Key> by lazy {
        (scopedBindings + scopedDependencies).keys
    }

    override
    fun newBuilder() = outKlass.classBuilder()

    override
    fun typeSpec() = typeSpec {
        addModifiers(PUBLIC, FINAL)
        addAnnotation(BootScope::class.java)
        val constructor = MethodSpec.constructorBuilder()
                .addAnnotation(Inject::class.java)
        val methods = scopedKeys.map { key ->
            val fieldName = key.fieldName
            val fieldSpec = addFieldSpec(key.type, fieldName)
            constructor.addToConstructor(fieldSpec, key.qualifier)
            key.getterMethod {
                addModifiers(PUBLIC, FINAL)
                // TODO: if any providers
//                addStatement("return \$N.get()", fieldSpec)
                addStatement("return \$N", fieldSpec)
            }
        }
        addMethods(methods.buildUnique())
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
