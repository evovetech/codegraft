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

package sourcerer.dev

import sourcerer.codegen.ApplicationComponentGenerator
import sourcerer.codegen.BootstrapBuilderGenerator
import sourcerer.codegen.ComponentBootDataGenerator
import sourcerer.codegen.ComponentImplementationGenerator
import sourcerer.codegen.ComponentModuleGenerator
import javax.annotation.processing.FilerException
import javax.inject.Inject

class BootstrapComponentStep
@Inject constructor(
    private val bootFactory: BootstrapBuilderGenerator.Factory,
    private val appFactory: ApplicationComponentGenerator.Factory,
    private val componentImplementationFactory: ComponentImplementationGenerator.Factory,
    private val componentModuleFactory: ComponentModuleGenerator.Factory,
    private val componentBootDataFactory: ComponentBootDataGenerator.Factory
) {
    fun process(
        bootstrapComponents: List<ComponentDescriptor>
    ): List<Output> = try {
        bootstrapComponents.map { descriptor ->
            Output(
                bootstrapBuilder = bootFactory.create(descriptor),
                oldApplicationComponent = appFactory.create(descriptor),
                component = ComponentOutput.create(this, descriptor)
            )
        }
    } catch (_: FilerException) {
        emptyList()
    }

    data
    class Output(
        val bootstrapBuilder: BootstrapBuilderGenerator,
        val oldApplicationComponent: ApplicationComponentGenerator,
        val component: ComponentOutput
    ) {
        val outputs: List<sourcerer.Output> = listOf(
            bootstrapBuilder,
            oldApplicationComponent,
            component.implementation,
            component.module,
            component.bootData
        )
    }

    data
    class ComponentOutput(
        val implementation: ComponentImplementationGenerator,
        val module: ComponentModuleGenerator,
        val bootData: ComponentBootDataGenerator
    ) {
        companion object {
            fun create(
                step: BootstrapComponentStep,
                descriptor: ComponentDescriptor
            ): ComponentOutput {
                val implementation = step.componentImplementationFactory.create(descriptor)
                return ComponentOutput(
                    implementation = implementation,
                    module = step.componentModuleFactory.create(descriptor, implementation),
                    bootData = step.componentBootDataFactory.create(descriptor)
                )
            }
        }
    }
}
