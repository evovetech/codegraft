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

import com.google.common.collect.ImmutableList
import dagger.MembersInjector
import sourcerer.AnnotationStep
import sourcerer.RoundStep
import sourcerer.processor.ProcessingEnv
import sourcerer.toImmutableList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoundSteps(
    steps: ImmutableList<RoundStep> = ImmutableList.of()
) : sourcerer.RoundSteps(steps) {
    @Inject constructor(
        env: ProcessingEnv,
        steps: AnnotationSteps,
        injector: MembersInjector<AnnotationStep>
    ) : this(steps.map { step ->
        injector.injectMembers(step)
        RoundStep(env, step)
    }.toImmutableList())
}

fun Collection<RoundStep>.toRoundSteps(): RoundSteps =
    RoundSteps(toImmutableList())
