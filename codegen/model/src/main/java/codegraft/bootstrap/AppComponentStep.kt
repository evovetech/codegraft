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

package codegraft.bootstrap

import codegraft.android.AndroidInjectModuleDescriptor
import codegraft.plugins.Modules
import com.google.common.collect.ImmutableSet
import dagger.internal.codegen.AppComponentGenerator
import dagger.internal.codegen.BootstrapComponentDescriptor
import javax.inject.Inject

class AppComponentStep
@Inject internal
constructor(
    val factory: AppComponentGenerator.Factory
) {
    internal
    fun process(
        generatedPluginModules: Modules,
        storedPluginModules: Modules,
        generatedModules: ImmutableSet<AndroidInjectModuleDescriptor>,
        storedModules: ImmutableSet<AndroidInjectModuleDescriptor>,
        generatedComponents: ImmutableSet<BootstrapComponentDescriptor>,
        storedComponents: ImmutableSet<BootstrapComponentDescriptor>
    ) = listOf(
        Output(
            factory.create(
                generatedPluginModules = generatedPluginModules,
                storedPluginModules = storedPluginModules,
                generatedModules = generatedModules,
                storedModules = storedModules,
                generatedComponents = generatedComponents,
                storedComponents = storedComponents
            )
        )
    )

    data
    class Output
    internal constructor(
        val appComponentGenerator: AppComponentGenerator
    ) {
        val outputs: List<sourcerer.Output> = listOf(
            appComponentGenerator.process()
        ).flatMap {
            it
        }
    }
}
