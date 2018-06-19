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

@Singleton
class CrashesComponentImpl
@Inject constructor(
    private val bootData: BootData,
    private val crashlyticsProvider: Provider<Crashlytics>,
    private val sampleInjector: MembersInjector<Sample>
) : CrashesComponent {
    override
    val app: AndroidApplication
        get() = bootData.app

    override
    val fabric: Fabric
        get() = bootData.fabric

    override
    val crashlytics: Crashlytics
        get() = crashlyticsProvider.get()

    override
    fun inject(
        sample: Sample
    ) = sampleInjector.injectMembers(sample)
}

@Module(includes = [Crashes::class])
interface CrashesComponentImplModule {
    @Binds
    fun bindCrashesComponent(impl: CrashesComponentImpl): CrashesComponent
}

@Module(includes = [CrashesComponentImplModule::class])
class BootData
@Inject constructor(
    @get:Provides
    @Singleton
    val app: AndroidApplication,

    @get:Provides
    @Singleton
    val fabric: Fabric
)

// package component
// application generated
@Singleton
@Component(
    modules = [
        BootData::class,
        Crashes::class,
        CrashesComponentImplModule::class
    ]
)
//@ApplicationComponent(includes = [CrashesComponent_ApplicationComponent::class])
interface AppComponent {
    val crashesComponent: CrashesComponent

    @Component.Builder
    abstract
    class Builder {
        @Inject
        fun injectMembers(
            bootData: BootData,
            crashes: Crashes?
        ) {
            bootData(bootData)
            crashes?.let {
                crashes(it)
            }
        }

        abstract
        fun crashes(crashes: Crashes)

        abstract
        fun bootData(bootData: BootData)

        abstract
        fun build(): AppComponent
    }
}

@Module(includes = [CrashesBootstrapModule::class])
class BootModule {
    @Provides
    @BootScope
    fun provideComponent(
        injector: MembersInjector<AppComponent.Builder>
    ): AppComponent {
        return DaggerAppComponent.builder().run {
            injector.injectMembers(this)
            build()
        }
    }
}

@BootScope
@Component(modules = [BootModule::class])
interface BootComponent {
    val component: AppComponent

    fun inject(builder: AppComponent.Builder)

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
