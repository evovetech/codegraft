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
import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.android.DaggerApplication
import io.realm.Realm
import io.realm.RealmConfiguration

class App : DaggerApplication() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        val config = RealmConfiguration.Builder()
                .name("app.realm")
                .schemaVersion(1)
                .build()
        Realm.setDefaultConfiguration(config)
    }

    override
    fun applicationInjector(): AppComponent {
        return DaggerAppComponent.builder()
                .app(this)
                .build()
    }
}

@Module
class Activities

@Module
abstract class AppModule {
    @Binds abstract fun bindContext(app: App): Context
    @Binds abstract fun bindApplication(app: App): Application
}