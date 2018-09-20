/*
 * Copyright (C) 2018 evove.tech
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package sourcerer

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
