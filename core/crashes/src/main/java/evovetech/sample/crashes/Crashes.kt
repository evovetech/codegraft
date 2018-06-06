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

package evovetech.sample.crashes

import android.app.Application
import com.crashlytics.android.Crashlytics
import dagger.Module
import dagger.Provides
import io.fabric.sdk.android.Fabric
import sourcerer.inject.BootstrapComponent
import sourcerer.inject.BootstrapModule
import sourcerer.inject.Builds
import sourcerer.inject.Generates
import sourcerer.inject.Initializes
import javax.inject.Singleton

@BootstrapComponent(
    bootstrapModules = [CrashesBootstrapModule::class],
    daggerModules = [Crashes::class]
)
interface CrashesComponent {
    val app: Application
    val crashlytics: Crashlytics
}

@BootstrapModule
object CrashesBootstrapModule {
    @JvmStatic
    @Generates
    fun generateFabricBuilder(app: Application): Fabric.Builder {
        return Fabric.Builder(app)
    }

    @JvmStatic
    @Builds
    fun buildFabric(builder: Fabric.Builder): Fabric {
        return builder.build()
    }

    @JvmStatic
    @Initializes
    fun initializeFabric(fabric: Fabric): Fabric {
        return Fabric.with(fabric)
    }
}

@Module
class Crashes {
    @Provides
    @Singleton
    fun provideCrashlytics(kits: Kits): Crashlytics {
        return kits[Crashlytics::class]
    }
}
