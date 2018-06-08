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
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import dagger.android.ContributesAndroidInjector
import evovetech.sample.crashes.Crashes
import evovetech.sample.crashes.CrashesBootstrapModule
import evovetech.sample.crashes.CrashesComponent_ApplicationComponent
import evovetech.sample.crashes.CrashesComponent_BootstrapBuilder
import evovetech.sample.db.realm.RealmBootstrapModule
import evovetech.sample.db.realm.RealmComponent_ApplicationComponent
import evovetech.sample.db.realm.RealmComponent_BootstrapBuilder
import evovetech.sample.db.realm.RealmModule
import evovetech.sample.network.ClientComponent_ApplicationComponent
import evovetech.sample.network.ClientPlugin
import io.fabric.sdk.android.Fabric
import io.realm.RealmConfiguration
import sourcerer.inject.ActivityScope
import sourcerer.inject.Boot
import sourcerer.inject.BootComponent
import sourcerer.inject.BootScope
import javax.inject.Singleton

//
// Generated
//

// package component
// application generated

// TODO:
@Module(
    includes = [
        AndroidInjectionModule::class
    ]
)
interface MainActivityModule {
    @ActivityScope
    @ContributesAndroidInjector
    fun contributeMainActivity(): MainActivity
}

@Singleton
@Component(
    modules = [
        ClientPlugin::class,
        RealmModule::class,
        Crashes::class,
        MainActivityModule::class
    ]
)
interface AppComponent :
    RealmComponent_ApplicationComponent,
    CrashesComponent_ApplicationComponent,
    ClientComponent_ApplicationComponent,
    AndroidInjector<App>,
    sourcerer.inject.AppComponent<App> {

    // TODO:
    fun inject(mainActivity: MainActivity)

    @Component.Builder
    interface Builder :
        RealmComponent_ApplicationComponent.Builder,
        CrashesComponent_ApplicationComponent.Builder,
        ClientComponent_ApplicationComponent.Builder {
        fun build(): AppComponent
    }
}

@Module(
    includes = [
        RealmBootstrapModule::class,
        CrashesBootstrapModule::class
    ]
)
class BootModule {
    @Provides
    @BootScope
    fun provideComponent(
        app: Application,
        fabric: Fabric,
        realmConfiguration: RealmConfiguration,
        crashes: Crashes?,
        realmModule: RealmModule?
    ): AppComponent = DaggerAppComponent.builder().run {
        application(app)
        fabric(fabric)
        realmConfiguration(realmConfiguration)
        crashes?.let {
            crashes(it)
        }
        realmModule?.let {
            realmModule(it)
        }
        build()
    }
}

@BootScope
@Component(modules = [BootModule::class])
interface AndroidBootComponent : BootComponent<AppComponent> {
    override val component: AppComponent

    @Component.Builder
    interface Builder :
        BootComponent.Builder<AndroidBootComponent>,
        RealmComponent_BootstrapBuilder,
        CrashesComponent_BootstrapBuilder {
        override fun build(): AndroidBootComponent
    }
}

typealias BootstrapInit = AndroidBootComponent.Builder.() -> App
typealias BootstrapBuilder = Boot.Builder<App, AppComponent>

class Bootstrap(
    boot: BootstrapInit
) : Boot<AppComponent>(boot::bootstrap)

private
fun BootstrapInit.bootstrap(): BootstrapBuilder {
    val builder = DaggerAndroidBootComponent.builder()
    return invoke(builder).let { app ->
        builder.application(app)
        BootstrapBuilder(app, builder)
    }
}
