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

package evovetech.sample

import android.app.Application
import dagger.Binds
import dagger.Module
import dagger.Subcomponent
import dagger.android.AndroidInjector
import dagger.multibindings.IntoMap
import evovetech.sample.MainApplicationModule.MainApplicationSubcomponent
import sourcerer.inject.android.AndroidInjectActivityModule

@Module(
    // TODO:
    includes = [AndroidInjectActivityModule::class],
    subcomponents = [MainApplicationSubcomponent::class]
)
interface MainApplicationModule {
    @Binds
    @IntoMap
    @ApplicationKey(App::class)
    fun bindAndroidInjectorFactory(
        builder: MainApplicationSubcomponent.Builder
    ): AndroidInjector.Factory<out Application>

    @Subcomponent
    interface MainApplicationSubcomponent : AndroidInjector<App> {
        @Subcomponent.Builder
        abstract class Builder : AndroidInjector.Builder<App>()
    }
}
