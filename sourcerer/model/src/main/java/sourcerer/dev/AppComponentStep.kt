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
import sourcerer.codegen.AppComponentGenerator
import javax.annotation.processing.FilerException
import javax.inject.Inject

class AppComponentStep
@Inject constructor(
    val factory: AppComponentGenerator.Factory
) {
    fun process(
        applicationComponents: List<ComponentDescriptor>
    ): List<Output> = try {
        val output = factory.create(applicationComponents)
        listOf(output)
    } catch (_: FilerException) {
        emptyList()
    }
}
