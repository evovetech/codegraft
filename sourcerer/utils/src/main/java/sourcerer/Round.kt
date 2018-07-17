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

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import com.google.common.collect.ImmutableSetMultimap
import sourcerer.processor.ProcessingEnv
import javax.lang.model.element.Element

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

