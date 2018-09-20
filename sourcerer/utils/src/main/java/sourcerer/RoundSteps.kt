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
import javax.annotation.processing.RoundEnvironment

abstract
class RoundSteps(
    steps: ImmutableList<RoundStep> = ImmutableList.of()
) : List<RoundStep> by steps {
    open
    fun preRound(parent: ParentRound) {
        forEach { step ->
            step.preRound(parent)
        }
    }

    open
    fun postRound(roundEnv: RoundEnvironment): List<Round> {
        return map { step ->
            val (extraOutputs, extraDeferredElements) = step.postRound(roundEnv)
            val currentRound = step.currentRound
            val outputs = currentRound.outputs + extraOutputs
            val deferredElements = currentRound.deferredElements + extraDeferredElements
            currentRound.copy(
                outputs = outputs.toRoundOutputs(),
                deferredElements = deferredElements.toImmutableSet()
            )
        }
    }
}
