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

package sourcerer.bootstrap

import com.google.auto.common.BasicAnnotationProcessor
import sourcerer.RoundStep
import sourcerer.processor.ProcessingEnv.Option
import javax.annotation.processing.RoundEnvironment
import javax.inject.Inject
import javax.lang.model.SourceVersion

class ProcessStepsDelegate
@Inject constructor(
    private val steps: RoundSteps
) : BasicAnnotationProcessor() {
    override
    fun initSteps(): Iterable<ProcessingStep> {
        return steps
    }

    override
    fun postRound(roundEnv: RoundEnvironment) {
        steps.forEach { step ->
            step.postRound(roundEnv)
        }
    }

    override
    fun getSupportedSourceVersion() =
        SourceVersion.latestSupported()!!

    override
    fun getSupportedOptions(): Set<String> = steps
            .flatMap(RoundStep::supportedOptions)
            .map(Option::key)
            .toSet()
}
