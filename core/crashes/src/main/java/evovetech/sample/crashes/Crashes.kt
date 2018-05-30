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

@file:Module
@file:JvmName("Crashes")

package evovetech.sample.crashes

import android.app.Application
import com.crashlytics.android.Crashlytics
import dagger.Module
import dagger.Provides
import io.fabric.sdk.android.Fabric
import javax.inject.Singleton

@Provides
@Singleton
fun provideFabric(app: Application, builder: CrashBuilder): Fabric {
    val fabric = builder.run {
        Fabric.Builder(app).run {
            init()
            build()
        }
    }
    return Fabric.with(fabric)
}

@Provides
@Singleton
fun provideCrashlytics(kits: Kits): Crashlytics {
    return kits[Crashlytics::class]
}
