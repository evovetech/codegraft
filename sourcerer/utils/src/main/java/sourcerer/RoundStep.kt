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

import com.google.auto.common.BasicAnnotationProcessor.ProcessingStep
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import sourcerer.processor.ProcessingEnv
import sourcerer.processor.ProcessingEnv.Option
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element

class RoundStep(
    private val env: ProcessingEnv,
    internal val step: AnnotationStep
) : ProcessingStep {
    private
    var _currentRound = Round()

    private lateinit
    var _parentRound: ParentRound

    val currentRound: Round
        get() = _currentRound

    fun supportedOptions(): Iterable<Option> =
        step.supportedOptions()

    override
    fun annotations(): Set<Class<out Annotation>> = step.annotations()
            .map { it.java }
            .toSet()

    fun preRound(parentRound: ParentRound) {
        this._parentRound = parentRound
    }

    override
    fun process(
        annotationElements: AnnotationElements
    ): ImmutableSet<Element> = _currentRound.process(_parentRound, env, step, annotationElements).let { nextRound ->
        _currentRound = nextRound
        nextRound.deferredElements
    }

    fun postRound(roundEnv: RoundEnvironment): Pair<Outputs, ImmutableList<Element>> {
        val nextRound = _currentRound.catchupTo(_parentRound)
        _currentRound = nextRound

        if (roundEnv.processingOver()) {
            return Pair(emptyList(), ImmutableList.of())
        }

        val outputs = step.postRound(roundEnv)
        val deferredElements = outputs.mapNotNull(env::mapOutput)
                .toImmutableList()
        return Pair(outputs, deferredElements)
    }
}
