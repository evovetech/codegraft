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

    fun getEnv(): ProcessingEnv {
        return env
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
    fun postRound(roundEnv: RoundEnvironment): Outputs {
        // subclass override
        return emptyList()
    }

    fun annotations(): Set<AnnotationType> = env.annotations()

    fun process(annotationElements: AnnotationElements): Outputs =
        env.process(annotationElements)
}
