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
