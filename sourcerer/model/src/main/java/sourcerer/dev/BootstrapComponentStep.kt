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

import sourcerer.Output
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
        bootstrapComponents.flatMap { descriptor ->
            val boot = bootFactory.create(descriptor)
            val app = appFactory.create(descriptor)
            val componentImplementation = componentImplementationFactory.create(descriptor)
            val componentModule = componentModuleFactory.create(descriptor, componentImplementation)
            val componentBootData = componentBootDataFactory.create(descriptor)
            listOf(
                boot,
                app,
                componentImplementation,
                componentModule,
                componentBootData
            )
        }
    } catch (_: FilerException) {
        emptyList()
    }
}
