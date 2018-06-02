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
import evovetech.sample.crashes.CrashesComponent_BootstrapComponent
import io.fabric.sdk.android.Fabric
import io.realm.RealmConfiguration
import sourcerer.inject.BootScope
import sourcerer.inject.FunctionQualifier
import javax.inject.Inject
import javax.inject.Singleton

//
// Generated
//

@Singleton
@Component(modules = [RealmModule::class])
interface RealmComponent_BootstrapComponent : RealmComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance fun application(app: Application)
        @BindsInstance fun realmConfiguration(realmConfiguration: RealmConfiguration)
        fun build(): RealmComponent_BootstrapComponent
    }
}

// group component
@BootScope
class LibraryComponent
@Inject constructor(
    val crashesComponent: CrashesComponent_BootstrapComponent,
    val realmComponent: RealmComponent_BootstrapComponent
) : CrashesComponent_BootstrapComponent by crashesComponent,
    RealmComponent_BootstrapComponent by realmComponent

@Module(includes = [evovetech.sample.crashes.BootMod::class])
class BootMod {
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
    fun provideRealmComponent(
        app: Application,
        realmConfiguration: RealmConfiguration
    ): RealmComponent_BootstrapComponent = DaggerRealmComponent_BootstrapComponent.builder().run {
        application(app)
        realmConfiguration(realmConfiguration)
        build()
    }
}

@BootScope
@Component(modules = [BootMod::class])
interface BootComp {
    val component: LibraryComponent

    @Component.Builder
    interface Builder {
        @BindsInstance fun app(app: Application): Builder

        @BindsInstance fun realm(
            @FunctionQualifier(
                params = [RealmConfiguration.Builder::class],
                returnType = [RealmConfiguration::class]
            ) init: (RealmConfiguration.Builder) -> RealmConfiguration
        ): Builder

        @BindsInstance fun fabric(
            @FunctionQualifier(
                params = [Fabric.Builder::class],
                returnType = [Fabric::class]
            ) init: (Fabric.Builder) -> Fabric
        ): Builder

        fun build(): BootComp
    }
}
