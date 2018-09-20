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
