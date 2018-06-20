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
import sourcerer.codegen.ComponentImplementationGenerator
import javax.annotation.processing.FilerException
import javax.inject.Inject

class BootstrapComponentStep
@Inject constructor(
    private val bootFactory: BootstrapBuilderGenerator.Factory,
    private val appFactory: ApplicationComponentGenerator.Factory,
    private val componentImplFactory: ComponentImplementationGenerator.Factory
) {
    fun process(
        bootstrapComponents: List<ComponentDescriptor>
    ): List<Output> = try {
        bootstrapComponents.flatMap {
            val boot = bootFactory.create(it)
            val app = appFactory.create(it)
            val compImpl = componentImplFactory.create(it)
            listOf(boot, app, compImpl)
        }
    } catch (_: FilerException) {
        emptyList()
    }
}
