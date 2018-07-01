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

import android.content.Context
import dagger.Module
import dagger.Provides
import sourcerer.inject.BootScope
import sourcerer.inject.BootstrapComponent

@BootstrapComponent(
    bootstrapModules = [
        AndroidBootModule::class
    ],
    applicationModules = [
        MainActivityModule::class,
        MainApplicationModule::class
    ]
)
interface MainAppComponent {
//    fun inject(application: App)
}

class AndroidAppComponent(
    delegate: AppComponent
) : sourcerer.inject.android.AppComponent<App>,
    AppComponent by delegate {
    override
    fun inject(application: App) {
        hasApplicationInjector.applicationInjector.inject(application)
    }
}

class AndroidBootComponent(
    delegate: BootComponent
) : sourcerer.inject.android.BootComponent<AndroidAppComponent>,
    BootComponent by delegate {
    override
    val component = AndroidAppComponent(
        delegate.appComponent
    )
}

@Module
class AndroidBootModule {
    // TODO: @Binds
    @Provides @BootScope
    fun provideContext(@BootScope app: App): Context {
        return app
    }
}
