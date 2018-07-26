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

import codegraft.bootstrap.Dependency
import codegraft.bootstrap.fieldName
import codegraft.bootstrap.getterMethod
import codegraft.bootstrap.qualifier
import codegraft.bootstrap.type
import com.google.common.collect.ImmutableSet
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import dagger.model.DependencyRequest
import dagger.model.Key
import sourcerer.JavaOutput
import sourcerer.addFieldSpec
import sourcerer.addToConstructor
import sourcerer.buildUnique
import sourcerer.classBuilder
import sourcerer.toImmutableSet
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
    private val descriptor: BootstrapComponentDescriptor
) : JavaOutput(
    rawType = ClassName.get(descriptor.componentDefinitionType),
    outExt = "BootData"
) {
    private val bindings: ImmutableSet<ContributionBinding> by lazy {
        descriptor.modules.flatMap(ModuleDescriptor::bindings)
                .toImmutableSet()
    }
    private val scopedBindings: ImmutableSet<ContributionBinding> by lazy {
        bindings.filter { it.scope().isPresent }
                .toImmutableSet()
    }
    private val dependencies: ImmutableSet<DependencyRequest> by lazy {
        descriptor.modules.flatMap(ModuleDescriptor::dependencies)
                .toImmutableSet()
    }
    private val scopedDependencies: ImmutableSet<DependencyRequest> by lazy {
        dependencies.filter {
            it.requestElement().map {
                it.uniqueScope
            }.isPresent
        }.toImmutableSet()
    }

    val scopedKeys: ImmutableSet<Key> by lazy {
        val bindingKeys = scopedBindings.keys
        val depKeys = scopedDependencies.map(Dependency::key)
        (bindingKeys + depKeys)
                .toImmutableSet()
    }

    override
    val include: Boolean by lazy {
        scopedKeys.isNotEmpty()
    }

    override
    fun newBuilder() = outKlass.classBuilder()

    override
    fun typeSpec() = typeSpec {
        addModifiers(PUBLIC, FINAL)
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
    @Inject constructor() {
        fun create(
            descriptor: BootstrapComponentDescriptor
        ) = ComponentBootDataGenerator(
            descriptor = descriptor
        )
    }
}
