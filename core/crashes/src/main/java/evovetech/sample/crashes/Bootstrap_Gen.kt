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

package evovetech.sample.crashes

import android.app.Application
import com.crashlytics.android.Crashlytics
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.MembersInjector
import dagger.Module
import dagger.Provides
import evovetech.sample.crashes.AppComponent.Builder
import io.fabric.sdk.android.Fabric
import sourcerer.inject.BootScope
import sourcerer.inject.android.AndroidApplication
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

//
// Generated
//

// TODO:
// @ApplicationComponent(modules=[CrashesComponent_Module::class])
//
@Singleton
class CrashesComponent_Implementation
@Inject constructor(
    private val appProvider: Provider<AndroidApplication>,
    private val fabricProvider: Provider<Fabric>,
    private val crashlyticsProvider: Provider<Crashlytics>,
    private val sampleInjector: MembersInjector<Sample>
) : CrashesComponent {
    override
    val app: AndroidApplication
        get() = appProvider.get()

    override
    val fabric: Fabric
        get() = fabricProvider.get()

    override
    val crashlytics: Crashlytics
        get() = crashlyticsProvider.get()

    override
    fun inject(sample: Sample) =
        sampleInjector.injectMembers(sample)
}

@Module(includes = [Crashes::class])
interface CrashesComponent_Module {
    @Binds
    fun bindCrashesComponent(
        implementation: CrashesComponent_Implementation
    ): CrashesComponent
}

@BootScope
class CrashesComponent_BootData
@Inject constructor(
    val app: AndroidApplication,
    val fabric: Fabric
)

// package component
// application generated

@Module(includes = [CrashesComponent_Module::class])
class AppComponent_BootData
@Inject constructor(
    @get:Provides
    val crashes: CrashesComponent_BootData
) {
    val app: AndroidApplication
        @Provides @Singleton
        get() = crashes.app

    val fabric: Fabric
        @Provides @Singleton
        get() = crashes.fabric
}

@Singleton
@Component(modules = [AppComponent_BootData::class])
interface AppComponent {
    val crashesComponent: CrashesComponent

    @Component.Builder
    interface Builder {
        fun bootData(bootData: AppComponent_BootData)
        fun crashes(crashes: Crashes)
        fun build(): AppComponent
    }
}

class AppComponent_Builder
private constructor(
    private val actual: Builder
) {
    @Inject constructor(
        bootData: AppComponent_BootData,
        crashes: Crashes?
    ) : this(
        actual = DaggerAppComponent.builder()
    ) {
        actual.bootData(bootData)
        crashes?.let {
            actual.crashes(it)
        }
    }

    fun build(): AppComponent {
        return actual.build()
    }
}

@Module(includes = [CrashesBootstrapModule::class])
class BootModule {
    @Provides @BootScope
    fun provideComponent(
        builder: AppComponent_Builder
    ): AppComponent = builder.build()
}

@BootScope
@Component(modules = [BootModule::class])
interface BootComponent {
    val component: AppComponent

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun crashes(crashes: Crashes?)

        @BindsInstance
        fun application(application: Application)

        @BindsInstance
        fun function1(@Named("fabric") function1: Function1<Fabric.Builder, Fabric>)

        fun build(): BootComponent
    }
}
