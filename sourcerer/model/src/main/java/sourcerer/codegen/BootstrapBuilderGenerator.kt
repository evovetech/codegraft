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
import dagger.BindsInstance
import org.jetbrains.annotations.Nullable
import sourcerer.Env
import sourcerer.JavaOutput
import sourcerer.addAnnotation
import sourcerer.addTo
import sourcerer.dev.ComponentDescriptor
import sourcerer.inject.BootstrapBuilder
import sourcerer.interfaceBuilder
import sourcerer.toKlass
import sourcerer.typeSpec
import javax.inject.Inject
import javax.lang.model.element.Modifier.ABSTRACT
import javax.lang.model.element.Modifier.PUBLIC

class BootstrapBuilderGenerator(
    private val env: Env,
    private val descriptor: ComponentDescriptor
) : JavaOutput(
    rawType = ClassName.get(descriptor.definitionType),
    outExt = "BootstrapBuilder"
) {
    override
    fun newBuilder() = outKlass.interfaceBuilder()

    override
    fun typeSpec() = typeSpec {
        addModifiers(PUBLIC)
        addAnnotation(ClassName.get(BootstrapBuilder::class.java).toKlass()) {
            descriptor.modules
                    .mapNotNull { ClassName.get(it.definitionType) }
                    .forEach(addTo("modules"))
        }

        // add method for each module
        val applicationModules = descriptor.applicationModules.map { module ->
            val param = module.definitionType.buildParameter {
                addAnnotation(Nullable::class.java)
            }
            MethodBuilder(param.name) {
                addAnnotation(BindsInstance::class.java)
                addModifiers(PUBLIC, ABSTRACT)
                addParameter(param)
            }
        }
        env.log("applicationModules = $applicationModules")

        val dependencies = descriptor.modules.flatMap { it.dependencies }
        val dependencyMethods = dependencies.map { dep ->
            val param = dep.buildParameter()
            MethodBuilder(param.name) {
                addAnnotation(BindsInstance::class.java)
                addModifiers(PUBLIC, ABSTRACT)
                addParameter(param)
            }
        }
        env.log("dependencyMethods = $dependencyMethods")

        (applicationModules + dependencyMethods)
                .buildUnique()
                .map(this::addMethod)
    }

    class Factory
    @Inject constructor(
        private val env: Env
    ) {
        fun create(descriptor: ComponentDescriptor): BootstrapBuilderGenerator {
            return BootstrapBuilderGenerator(env, descriptor)
        }
    }
}
