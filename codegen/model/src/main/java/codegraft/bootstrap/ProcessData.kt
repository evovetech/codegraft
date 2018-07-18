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

package codegraft.bootstrap

import sourcerer.ExactSet
import sourcerer.processor.ProcessingEnv
import sourcerer.toImmutableList
import javax.annotation.processing.Processor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProcessData
@Inject constructor(
    val env: ProcessingEnv,
    processors: ExactSet<Processor>,
    val steps: RoundSteps,
    val bootstrapComponentStep: BootstrapComponentStep,
    val androidInjectStep: AndroidInjectStep,
    val appComponentStep: AppComponentStep
) {
    val processors = processors
            .toImmutableList()
}