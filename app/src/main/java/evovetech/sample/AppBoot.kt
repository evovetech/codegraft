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

import com.crashlytics.android.Crashlytics
import evovetech.sample.AppComponent.Builder
import evovetech.sample.crashes.crashes
import evovetech.sample.db.realm.defaultRealm

class AppBoot : Bootstrap<App> {
    override
    fun Builder.onBoot(app: App) {
        app(app)
        crashes {
            kits(Crashlytics())
        }
        defaultRealm {
            name("app.realm")
            schemaVersion(1)
        }
    }

    fun build(app: App): AppComponent {
        val builder = DaggerAppComponent.builder()
        builder.onBoot(app)
        return builder.build()
    }
}
