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

