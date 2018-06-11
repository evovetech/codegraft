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

import android.content.ContentProvider
import android.util.Log
import com.crashlytics.android.Crashlytics
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import io.fabric.sdk.android.Fabric
import sourcerer.inject.android.BootApplication
import javax.inject.Inject

//@AndroidApplication(AppBoot::class)
class App : DaggerApplication(), BootApplication<AppComponent> {
    @Inject lateinit
    var fabric: Fabric

    override
    val bootstrap = bootstrap {
        fabric {
            kits(Crashlytics())
            build()
        }
        realm {
            name("app.realm")
            schemaVersion(1)
            build()
        }
        this@App
    }

    override
    fun onCreate() {
        super.onCreate()
        logStartup("onCreate")
        this::class.java.methods.find {
            it.name == "getDefined" && it.parameterTypes.isEmpty()
        }?.let {
            val value = it.invoke(this) as? String
            Log.d("onCreate", "defined=$value")
        } ?: Log.d("onCreate", "no method getDefined() is defined :(")
    }

    override
    fun contentProviderInjector(): AndroidInjector<ContentProvider> {
        return super.contentProviderInjector().also {
            logStartup("contentProviderInjector")
        }
    }

    override
    fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        throw IllegalStateException("shouldn't be called")
    }

    fun logStartup(tag: String) {
        Log.d(tag, "startup")
        fabric.kits.forEach {
            Log.d(tag, "app -- fabric kit=$it")
        }
    }
}
