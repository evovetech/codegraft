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

import android.content.Context
import dagger.BindsInstance
import dagger.Module
import dagger.Provides
import io.realm.Realm
import io.realm.RealmConfiguration
import sourcerer.inject.LibComponent
import sourcerer.inject.LibModule
import javax.inject.Singleton

//@LibModule(includes = [RealmModule::class])
//@LibComponent(modules = [RealmModule::class])
interface RealmComponent {
    val defaultRealm: Realm

    @LibComponent.Builder
    interface Builder {
        @BindsInstance
        fun defaultRealm(
            builder: RealmBuilder
        ): Builder
    }
}

@Module
class RealmModule {
    @Provides
    @Singleton
    fun defaultRealmConfiguration(context: Context, builder: RealmBuilder): RealmConfiguration {
        Realm.init(context)
        val config = builder.run {
            RealmConfiguration.Builder().run {
                init()
                build()
            }
        }
        Realm.setDefaultConfiguration(config)
        return config
    }

    @Provides
    fun defaultRealm(config: RealmConfiguration): Realm {
        return Realm.getInstance(config)
    }
}

interface RealmBuilder {
    fun RealmConfiguration.Builder.init()
}
