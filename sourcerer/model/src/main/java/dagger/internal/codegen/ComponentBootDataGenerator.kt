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
import dagger.model.Key
import sourcerer.JavaOutput
import sourcerer.bootstrap.Dependency
import sourcerer.bootstrap.fieldName
import sourcerer.bootstrap.getterMethod
import sourcerer.bootstrap.qualifier
import sourcerer.bootstrap.type
import sourcerer.classBuilder
import sourcerer.inject.BootScope
import sourcerer.typeSpec
import javax.inject.Inject
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PUBLIC

internal
val Collection<BindingDeclaration>.keys: Set<Key>
    get() = map(BindingDeclaration::key)
            .toSet()

internal
class ComponentBootDataGenerator(
    private val env: Env,
    private val types: SourcererTypes,
    private val elements: SourcererElements,
    private val descriptor: BootstrapComponentDescriptor
) : JavaOutput(
    rawType = ClassName.get(descriptor.componentDefinitionType),
    outExt = "BootData"
) {
    private val allBindings by lazy {
        descriptor.modules.flatMap(ModuleDescriptor::bindings)
    }
    private val scopedBindings by lazy {
        allBindings.filter { it.scope().isPresent }
    }
    private val allDependencies by lazy {
        descriptor.modules.flatMap(ModuleDescriptor::dependencies)
    }
    private val scopedDependencies by lazy {
        allDependencies.filter {
            it.requestElement().map {
                it.uniqueScope
            }.isPresent
        }
    }

    val scopedKeys: Set<Key> by lazy {
        val bindingKeys = scopedBindings.keys
        val depKeys = scopedDependencies.map(Dependency::key)
                .toSet()
        bindingKeys + depKeys
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
            descriptor: BootstrapComponentDescriptor
        ) = ComponentBootDataGenerator(
            env = env,
            types = types,
            elements = elements,
            descriptor = descriptor
        )
    }
}
