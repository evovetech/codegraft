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

import android.app.Activity
import android.app.Application
import android.util.Log
import com.crashlytics.android.Crashlytics
import dagger.android.AndroidInjector
import dagger.android.HasActivityInjector
import io.fabric.sdk.android.Fabric
import io.realm.RealmConfiguration
import sourcerer.inject.android.BootApplication
import sourcerer.inject.android.component
import javax.inject.Inject

//@AndroidApplication(AppBoot::class)
class App : Application(),
    BootApplication<AndroidAppComponent>,
    HasActivityInjector {
    @Inject lateinit
    var fabric: Fabric

    override
    val bootstrap = bootstrap {
        fabricBuilderFunction1(Fabric.Builder::bootstrap)
        realmConfigurationBuilderFunction1(RealmConfiguration.Builder::bootstrap)
        this@App
    }

    override
    fun onCreate() {
        super.onCreate()
        logStartup("onCreate")
        this::class.java.fields.find {
            it.name == "defined"
        }?.let {
            val value = it.get(this) as? String
            Log.d("onCreate", "defined=$value")
        } ?: Log.d("onCreate", "no method getDefined() is defined :(")
    }

    override
    fun activityInjector(): AndroidInjector<Activity> =
        component.activityInjector

    fun logStartup(tag: String) {
        Log.d(tag, "startup")
        fabric.kits.forEach {
            Log.d(tag, "app -- fabric kit=$it")
        }
    }
}

fun Fabric.Builder.bootstrap(): Fabric {
    return kits(Crashlytics())
            .build()
}

fun RealmConfiguration.Builder.bootstrap(): RealmConfiguration {
    return name("app.realm")
            .schemaVersion(1)
            .build()
}
