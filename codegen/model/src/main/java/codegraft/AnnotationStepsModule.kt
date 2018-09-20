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
import codegraft.bootstrap.BootstrapComponentStep
import codegraft.plugins.GeneratePluginBindingsStep
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import dagger.multibindings.Multibinds
import sourcerer.AnnotationStep
import sourcerer.EnvModule

typealias AnnotationSteps = Set<@JvmSuppressWildcards AnnotationStep>

@Module(includes = [EnvModule::class])
internal
interface AnnotationStepsModule {
    @Binds @IntoSet
    fun provideComponentStep(step: BootstrapComponentStep): AnnotationStep

    @Binds @IntoSet
    fun provideAndroidInjectStep(step: AndroidInjectStep): AnnotationStep

    @Binds @IntoSet
    fun provideGeneratePluginBindingsStep(step: GeneratePluginBindingsStep): AnnotationStep

    @Multibinds
    fun bindAnnotationSteps(): AnnotationSteps
}


