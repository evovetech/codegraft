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
import io.fabric.sdk.android.Fabric
import io.fabric.sdk.android.services.settings.IconRequest.build
import io.realm.RealmConfiguration
import sourcerer.inject.BootScope
import sourcerer.inject.FunctionQualifier
import javax.inject.Singleton

//
// Generated
//

interface RealmComponent_BootstrapComponent : RealmComponent {
    interface Builder {
        @BindsInstance fun application(app: Application)
        @BindsInstance fun realmConfiguration(realmConfiguration: RealmConfiguration)
    }
}

// group component
interface LibraryComponent : RealmComponent_BootstrapComponent, evovetech.sample.crashes.LibraryComponent {
    interface Builder :
        RealmComponent_BootstrapComponent.Builder,
        evovetech.sample.crashes.LibraryComponent.Builder
}

@Singleton
@Component(modules = [RealmModule::class, Crashes::class])
interface LibraryComponentImpl : LibraryComponent {
    @Component.Builder
    interface Builder : LibraryComponent.Builder {
        fun build(): LibraryComponent
    }
}

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
        fabric: Fabric,
        realmConfiguration: RealmConfiguration
    ): LibraryComponent = DaggerLibraryComponentImpl.builder().run {
        application(app)
        fabric(fabric)
        realmConfiguration(realmConfiguration)
        build()
    }
}

interface BootComp : evovetech.sample.crashes.BootComp {
    override val component: LibraryComponent

    interface Builder : evovetech.sample.crashes.BootComp.Builder {
        @BindsInstance fun realm(
            @FunctionQualifier(
                params = [RealmConfiguration.Builder::class],
                returnType = [RealmConfiguration::class]
            ) init: (RealmConfiguration.Builder) -> RealmConfiguration
        )
    }
}

@BootScope
@Component(modules = [BootMod::class])
interface BootCompImpl : BootComp {
    override val component: LibraryComponent

    @Component.Builder
    interface Builder : BootComp.Builder {
        fun build(): BootComp
    }
}
