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

package codegraft

import sourcerer.processor.ProcessingEnv
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement

open
class MainProcessor(
    protected val steps: ProcessStepsProcessor
) : Processor by steps {
    constructor(
        isApplication: Boolean
    ) : this(
        ProcessStepsProcessor(isApplication)
    )

    override
    fun getSupportedAnnotationTypes() =
        ProcessingEnv.ALL_ANNOTATION_TYPES

    override
    fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        steps.processingEnv.log("MainProcessor process($annotations)")
        steps.process(annotations, roundEnv)
        return false
    }
}
