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

package sourcerer.bootstrap

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import dagger.multibindings.Multibinds

typealias AnnotationSteps = Set<@JvmSuppressWildcards AnnotationStep>

@Module(includes = [EnvModule::class])
internal
interface AnnotationStepsModule {
    @Binds @IntoSet
    fun provideComponentStep(step: ComponentStep): AnnotationStep

    @Binds @IntoSet
    fun provideAndroidInjectStep(step: AndroidInjectStep): AnnotationStep

    @Multibinds
    fun bindAnnotationSteps(): AnnotationSteps
}


