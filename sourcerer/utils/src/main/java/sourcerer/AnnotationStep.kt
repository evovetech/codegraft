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

package sourcerer

import sourcerer.processor.ProcessingEnv
import sourcerer.processor.ProcessingEnv.Option
import javax.annotation.processing.RoundEnvironment
import javax.inject.Inject

abstract
class AnnotationStep {
    private lateinit
    var env: ProcessingEnv

    @Inject
    fun setEnv(env: ProcessingEnv) {
        this.env = env
    }

    protected abstract
    fun ProcessingEnv.annotations(): Set<AnnotationType>

    protected abstract
    fun ProcessingEnv.process(
        annotationElements: AnnotationElements
    ): Outputs

    open
    fun supportedOptions(): Iterable<Option> {
        // subclass override
        return emptyList()
    }

    open
    fun postRound(roundEnv: RoundEnvironment): Unit {
        // subclass override
    }

    fun annotations(): Set<AnnotationType> = env.annotations()

    fun process(annotationElements: AnnotationElements): Outputs =
        env.process(annotationElements)
}