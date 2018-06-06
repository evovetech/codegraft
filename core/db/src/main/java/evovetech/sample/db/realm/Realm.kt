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
import dagger.Module
import dagger.Provides
import evovetech.sample.crashes.CrashesComponent
import io.realm.Realm
import io.realm.RealmConfiguration
import sourcerer.inject.BootstrapComponent
import sourcerer.inject.Builds
import sourcerer.inject.Initializes

@BootstrapComponent(
    dependencies = [CrashesComponent::class],
    modules = [RealmModule::class]
)
interface RealmComponent {
    val realm: Realm
    val crashlytics: Crashlytics

    @BootstrapComponent.Builder
    object Builder {
        @JvmStatic
        @Builds(RealmConfiguration::class)
        fun realmConfigurationBuilder(app: Application): RealmConfiguration.Builder {
            Realm.init(app)
            return RealmConfiguration.Builder()
        }

        @JvmStatic
        @Initializes
        fun initializeRealmConfiguration(config: RealmConfiguration): RealmConfiguration {
            Realm.setDefaultConfiguration(config)
            return config
        }
    }
}

@Module
class RealmModule {
    @Provides
    fun provideRealm(config: RealmConfiguration): Realm {
        return Realm.getInstance(config)
    }
}
