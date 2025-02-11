/*
 * Copyright (C) 2018 evove.tech
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package evovetech.sample

import android.app.Application
import android.util.Log
import codegraft.inject.AndroidInject
import codegraft.inject.android.AndroidApplication
import codegraft.inject.android.BootApplication
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
//import io.realm.RealmConfiguration
import okhttp3.OkHttpClient
import javax.inject.Inject

@AndroidInject
class App : Application(), BootApplication<AppComponent> {
    @Inject lateinit
    var fabric: Fabric

    override
    val bootstrap = bootstrap {
        fabricBuilderFunction1(Fabric.Builder::bootstrap)
//        realmConfigurationBuilderFunction1(RealmConfiguration.Builder::bootstrap)
        okHttpClientApplicationBuilderFunction2(OkHttpClient.Builder::bootstrap)
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

//fun RealmConfiguration.Builder.bootstrap(): RealmConfiguration {
//    return name("app.realm")
//            .schemaVersion(1)
//            .build()
//}

fun OkHttpClient.Builder.bootstrap(app: AndroidApplication): OkHttpClient {
    // TODO
    return build()
}
