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

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import com.google.common.collect.ImmutableSetMultimap
import sourcerer.AnnotationElements
import sourcerer.Output
import sourcerer.mapOutput
import sourcerer.processor.ProcessingEnv
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

data
class ParentRound(
    val number: Int = 0,
    val prev: ParentRound? = null,
    val elements: ImmutableSet<TypeElement> = ImmutableSet.of(),
    val rootElements: ImmutableSet<Element> = ImmutableSet.of(),
    val rounds: ImmutableList<Round> = ImmutableList.of()
) {
    val outputs: ImmutableList<Output> by lazy {
        rounds.flatMap(Round::outputs)
                .toImmutableList()
    }
    val deferredElements: ImmutableSet<Element> by lazy {
        rounds.flatMap(Round::deferredElements)
                .toImmutableSet()
    }

    fun process(
        elements: Set<TypeElement>,
        roundEnv: RoundEnvironment,
        steps: RoundSteps,
        processFunc: () -> Boolean
    ): ParentRound {
        val round = ParentRound(
            number + 1,
            this,
            elements.toImmutableSet(),
            roundEnv.rootElements.toImmutableSet()
        )
        steps.preRound(round)

        processFunc()

        if (!roundEnv.processingOver()) {
            steps.postRound(roundEnv)
        }

        val rounds = steps.map { step ->
            step.currentRound.catchupTo(round)
        }.toImmutableList()
        return round.copy(
            rounds = rounds
        )
    }
}

data
class Round(
    val parent: ParentRound? = null,
    val number: Int = 0,
    val prev: Round? = null,
    val inputs: RoundInputs = RoundInputs(),
    val outputs: RoundOutputs = RoundOutputs(),
    val deferredElements: ImmutableSet<Element> = ImmutableSet.of()
) {

    fun process(
        parent: ParentRound,
        env: ProcessingEnv,
        step: AnnotationStep,
        annotationElements: AnnotationElements
    ): Round {
        val outputs = step.process(annotationElements)
        val roundInputs = annotationElements.toRoundInputs()
        val roundOutputs = outputs.toRoundOutputs()
        val deferredElements = completeRound(env, roundOutputs)
        return Round(
            parent,
            number + 1,
            this@Round,
            roundInputs,
            roundOutputs,
            deferredElements
        )
    }

    fun catchupTo(
        parent: ParentRound
    ): Round = if (number < parent.number) {
        Round(
            parent = parent,
            number = parent.number,
            prev = this
        )
    } else {
        this
    }

    private
    fun completeRound(
        env: ProcessingEnv,
        outputs: RoundOutputs
    ): ImmutableSet<Element> = outputs
            .onEach { env.log("output=$it") }
            .mapNotNull(env::mapOutput)
            .toImmutableSet()

    override fun toString(): String {
        return "Round(number=$number, outputs=$outputs, deferredElements=$deferredElements)"
    }
}

class RoundInputs(
    delegate: AnnotationElements = ImmutableSetMultimap.of()
) : AnnotationElements by ImmutableSetMultimap.copyOf(delegate) {
    // TODO:
}

fun AnnotationElements.toRoundInputs(): RoundInputs = when (this) {
    is RoundInputs -> this
    else -> RoundInputs(this)
}

typealias Outputs = List<Output>

class RoundOutputs(
    delegate: Outputs = ImmutableList.of()
) : Outputs by ImmutableList.copyOf(delegate) {
    // TODO:
}

fun Outputs.toRoundOutputs(): RoundOutputs = when (this) {
    is RoundOutputs -> this
    else -> RoundOutputs(this)
}

