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

package evovetech.sample.network

import android.app.Application
import com.crashlytics.android.Crashlytics
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import evovetech.sample.crashes.Crashes
import evovetech.sample.crashes.CrashesBootstrapModule
import evovetech.sample.crashes.CrashesComponent_ApplicationComponent
import evovetech.sample.crashes.CrashesComponent_BootstrapBuilder
import io.fabric.sdk.android.Fabric
import sourcerer.inject.BootScope
import sourcerer.inject.Plugins
import sourcerer.inject.android.AndroidApplication
import javax.inject.Singleton

//
// Generated
//

// package component
// application generated
@Singleton
@Component(modules = [ClientPlugin::class, Crashes::class])
interface AppComponent :
    ClientComponent_ApplicationComponent,
    CrashesComponent_ApplicationComponent {

    override val plugins: Plugins
    override val app: AndroidApplication
    override val fabric: Fabric
    override val crashlytics: Crashlytics

    @Component.Builder
    interface Builder :
        CrashesComponent_ApplicationComponent.Builder,
        ClientComponent_ApplicationComponent.Builder {

        override fun crashes(crashes: Crashes)

        @BindsInstance
        override fun fabric(fabric: Fabric)

        @BindsInstance
        override fun application(application: Application)

        fun build(): AppComponent
    }
}

@Module(
    includes = [
        CrashesBootstrapModule::class
    ]
)
class BootModule {
    @Provides
    @BootScope
    fun provideComponent(
        app: AndroidApplication,
        fabric: Fabric,
        crashes: Crashes?
    ): AppComponent = DaggerAppComponent.builder().run {
        application(app)
        fabric(fabric)
        crashes?.let {
            crashes(it)
        }
        build()
    }
}

@BootScope
@Component(modules = [BootModule::class])
interface BootComponent {
    val component: AppComponent

    @Component.Builder
    interface Builder :
        CrashesComponent_BootstrapBuilder {
        fun build(): BootComponent
    }
}
