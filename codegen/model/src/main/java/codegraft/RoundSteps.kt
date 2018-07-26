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