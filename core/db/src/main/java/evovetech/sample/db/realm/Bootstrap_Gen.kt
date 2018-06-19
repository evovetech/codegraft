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

package evovetech.sample.db.realm

import android.app.Application
import com.crashlytics.android.Crashlytics
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.MembersInjector
import dagger.Module
import dagger.Provides
import evovetech.sample.crashes.Crashes
import evovetech.sample.crashes.CrashesBootstrapModule
import evovetech.sample.crashes.CrashesComponent
import evovetech.sample.crashes.CrashesComponent_BootData
import evovetech.sample.crashes.CrashesComponent_Module
import io.fabric.sdk.android.Fabric
import io.realm.Realm
import io.realm.RealmConfiguration
import sourcerer.inject.BootScope
import sourcerer.inject.android.AndroidApplication
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

class Sample {
    @Inject lateinit
    var app: AndroidApplication
}

//
// Generated
//

// TODO:
// @ApplicationComponent(
//     dependencies=[CrashesComponent_Implementation::class],
//     modules=[RealmComponent_Module::class]
// )
//
@Singleton
class RealmComponent_Implementation
@Inject constructor(
    private val realmProvider: Provider<Realm>,
    private val crashlyticsProvider: Provider<Crashlytics>,
    private val sampleInjector: MembersInjector<Sample>
) : RealmComponent {
    override
    val realm: Realm
        get() = realmProvider.get()

    override
    val crashlytics: Crashlytics
        get() = crashlyticsProvider.get()

    override
    fun inject(sample: Sample) =
        sampleInjector.injectMembers(sample)
}

@Module(includes = [RealmModule::class])
interface RealmComponent_Module {
    @Binds
    fun bindRealmComponent(
        implementation: RealmComponent_Implementation
    ): RealmComponent
}

@BootScope
class RealmComponent_BootData
@Inject constructor(
    val app: AndroidApplication,
    val realmConfiguration: RealmConfiguration
)

@Module(
    includes = [
        CrashesComponent_Module::class,
        RealmComponent_Module::class
    ]
)
class AppComponent_BootData
@Inject constructor(
    @get:Provides val crashes: CrashesComponent_BootData,
    @get:Provides val realm: RealmComponent_BootData
) {
    val app: AndroidApplication
        @Provides @Singleton
        get() = crashes.app

    val fabric: Fabric
        @Provides @Singleton
        get() = crashes.fabric

    val realmConfiguration: RealmConfiguration
        @Provides @Singleton
        get() = realm.realmConfiguration
}

// package component
// application generated
@Singleton
@Component(
    modules = [
        AppComponent_BootData::class
    ]
)
interface AppComponent {
    val crashesComponent: CrashesComponent
    val realmComponent: RealmComponent

    @Component.Builder
    interface Builder {
        fun bootData(bootData: AppComponent_BootData)
        fun crashes(crashes: Crashes)
        fun realmModule(realmModule: RealmModule)
        fun build(): AppComponent
    }
}

class AppComponent_Builder
private constructor(
    private val actual: AppComponent.Builder
) : AppComponent.Builder by actual {
    @Inject constructor(
        bootData: AppComponent_BootData,
        crashes: Crashes?,
        realmModule: RealmModule?
    ) : this(
        actual = DaggerAppComponent.builder()
    ) {
        actual.bootData(bootData)
        crashes?.let(actual::crashes)
        realmModule?.let(actual::realmModule)
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
        fun function11(@Named("fabric") function1: Function1<Fabric.Builder, Fabric>)

        @BindsInstance
        fun realmModule(realmModule: RealmModule?)

        @BindsInstance
        fun function12(
            @Named("realmInit") function1: Function1<RealmConfiguration.Builder, RealmConfiguration>
        )

        fun build(): BootComponent
    }
}
