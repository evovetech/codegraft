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
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

data
class ParentRound(
    val number: Int = 0,
    val prev: ParentRound? = null,
    val elements: ImmutableSet<TypeElement> = ImmutableSet.of(),
    val rootElements: ImmutableSet<Element> = ImmutableSet.of(),
    val rounds: ImmutableList<Round> = ImmutableList.of(),
    val parentOutputs: ImmutableList<Output> = ImmutableList.of()
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
        val parentRound = ParentRound(
            number + 1,
            this,
            elements.toImmutableSet(),
            roundEnv.rootElements.toImmutableSet()
        )
        steps.preRound(parentRound)

        processFunc()

        val rounds = steps.postRound(roundEnv)
                .toImmutableList()
        return parentRound.copy(rounds = rounds)
    }
}
