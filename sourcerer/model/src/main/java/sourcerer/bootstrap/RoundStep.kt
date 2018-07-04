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

import com.google.auto.common.BasicAnnotationProcessor.ProcessingStep
import com.google.common.collect.ImmutableSet
import sourcerer.AnnotationElements
import sourcerer.processor.ProcessingEnv
import sourcerer.processor.ProcessingEnv.Option
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element

class RoundSteps(
    steps: ImmutableSet<RoundStep>
) : Set<RoundStep> by steps {
    fun preProcess(parentRound: ParentRound) {
        forEach { step ->
            step.preProcess(parentRound)
        }
    }
}

fun Collection<RoundStep>.toRoundSteps(): RoundSteps =
    RoundSteps(toImmutableSet())

class RoundStep(
    private val env: ProcessingEnv,
    private val step: AnnotationStep
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

    fun preProcess(parentRound: ParentRound) {
        this._parentRound = parentRound
    }

    override
    fun process(
        annotationElements: AnnotationElements
    ): ImmutableSet<Element> = _currentRound.process(_parentRound, env, step, annotationElements).let { nextRound ->
        _currentRound = nextRound
        nextRound.deferredElements
    }

    fun postRound(roundEnv: RoundEnvironment) {
        step.postRound(roundEnv)
        // TODO:
    }
}
