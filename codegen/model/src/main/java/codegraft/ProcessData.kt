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

import codegraft.android.AndroidInjectStep
import codegraft.bootstrap.AppComponentStep
import codegraft.bootstrap.BootstrapComponentStep
import codegraft.plugins.GeneratePluginBindingsStep
import sourcerer.ExactSet
import sourcerer.processor.ProcessingEnv
import sourcerer.toImmutableList
import javax.annotation.processing.Processor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProcessData
@Inject constructor(
    val env: ProcessingEnv,
    processors: ExactSet<Processor>,
    val steps: RoundSteps,
    val bootstrapComponentStep: BootstrapComponentStep,
    val androidInjectStep: AndroidInjectStep,
    val appComponentStep: AppComponentStep,
    val generatePluginBindingsStep: GeneratePluginBindingsStep
) {
    val processors = processors
            .toImmutableList()
}
