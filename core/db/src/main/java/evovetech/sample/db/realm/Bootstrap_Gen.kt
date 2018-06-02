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
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import evovetech.sample.crashes.Crashes
import evovetech.sample.crashes.CrashesComponent.Builder
import evovetech.sample.crashes.CrashesComponent_ApplicationComponent
import evovetech.sample.db.realm.RealmComponent.Builder.initializeRealmConfiguration
import evovetech.sample.db.realm.RealmComponent.Builder.realmConfigurationBuilder
import io.fabric.sdk.android.Fabric
import io.fabric.sdk.android.services.settings.IconRequest.build
import io.realm.RealmConfiguration
import sourcerer.inject.ApplicationComponent
import sourcerer.inject.BootScope
import sourcerer.inject.FunctionQualifier
import javax.inject.Singleton

//
// Generated
//

// individual component
// module generated
@ApplicationComponent(
    // TODO: maybe includes instead of dependencies
    dependencies = [CrashesComponent_ApplicationComponent::class],
    modules = [RealmModule::class]
)
interface RealmComponent_ApplicationComponent : RealmComponent {
    @ApplicationComponent.Builder
    interface Builder {
        @BindsInstance fun application(app: Application)
        @BindsInstance fun realmConfiguration(realmConfiguration: RealmConfiguration)
    }
}

// package component
// application generated
@Singleton
@Component(modules = [RealmModule::class, Crashes::class])
interface AppComponent :
    RealmComponent_ApplicationComponent,
    CrashesComponent_ApplicationComponent {

    @Component.Builder
    interface Builder :
        RealmComponent_ApplicationComponent.Builder,
        CrashesComponent_ApplicationComponent.Builder {

        fun build(): AppComponent
    }
}

@Module
class BootModule {
    @Provides
    @BootScope
    fun provideRealmConfiguration(
        app: Application,
        @FunctionQualifier(
            params = [RealmConfiguration.Builder::class],
            returnType = [RealmConfiguration::class]
        ) init: (RealmConfiguration.Builder) -> RealmConfiguration
    ): RealmConfiguration {
        val builder = realmConfigurationBuilder(app)
        val config = init(builder)
        return initializeRealmConfiguration(config)
    }

    @Provides
    @BootScope
    fun provideFabric(
        app: Application,
        @FunctionQualifier(
            params = [Fabric.Builder::class],
            returnType = [Fabric::class]
        ) init: (Fabric.Builder) -> Fabric
    ): Fabric {
        val builder = Builder.fabricBuilder(app)
        val fabric = init(builder)
        return Builder.initializeFabric(fabric)
    }

    @Provides
    @BootScope
    fun providePackageComponent(
        app: Application,
        fabric: Fabric,
        realmConfiguration: RealmConfiguration
    ): AppComponent = DaggerAppComponent.builder().run {
        application(app)
        fabric(fabric)
        realmConfiguration(realmConfiguration)
        build()
    }
}

@BootScope
@Component(modules = [BootModule::class])
interface BootComponent {
    val component: AppComponent

    @Component.Builder
    interface Builder {
        @BindsInstance fun application(app: Application)

        @BindsInstance fun fabric(
            @FunctionQualifier(
                params = [Fabric.Builder::class],
                returnType = [Fabric::class]
            ) init: (Fabric.Builder) -> Fabric
        )

        @BindsInstance fun realm(
            @FunctionQualifier(
                params = [RealmConfiguration.Builder::class],
                returnType = [RealmConfiguration::class]
            ) init: (RealmConfiguration.Builder) -> RealmConfiguration
        )

        fun build(): BootComponent
    }
}
