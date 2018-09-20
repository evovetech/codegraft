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
