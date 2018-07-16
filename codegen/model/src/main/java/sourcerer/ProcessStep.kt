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

import sourcerer.bootstrap.AnnotationStep
import sourcerer.processor.Env
import sourcerer.processor.ProcessingEnv.Option

interface ProcessStep {
    fun Env.annotations(): Set<AnnotationType>

    fun Env.process(
        annotationElements: AnnotationElements
    ): Map<AnnotationType, List<Output>>

    fun supportedOptions(): Iterable<Option> =
        emptySet()
}

fun AnnotationStep.toProcessStep(): ProcessStep = object : ProcessStep {
    override
    fun Env.annotations(): Set<AnnotationType> {
        return this@toProcessStep.annotations()
    }

    override
    fun Env.process(
        annotationElements: AnnotationElements
    ): Map<AnnotationType, List<Output>> {
        val outputs = this@toProcessStep.process(annotationElements)
        return outputs.groupBy {
            Annotation::class
        }
    }
}
