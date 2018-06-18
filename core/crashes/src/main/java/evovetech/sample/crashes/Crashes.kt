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

import com.crashlytics.android.Crashlytics
import dagger.Module
import dagger.Provides
import io.fabric.sdk.android.Fabric
import sourcerer.inject.BootScope
import sourcerer.inject.BootstrapComponent
import sourcerer.inject.android.AndroidApplication
import javax.inject.Named
import javax.inject.Singleton

typealias FabricInit = Fabric.Builder.() -> Fabric

@BootstrapComponent(
    bootstrapModules = [CrashesBootstrapModule::class],
    applicationModules = [Crashes::class]
)
interface CrashesComponent {
    val app: AndroidApplication
    val fabric: Fabric
    val crashlytics: Crashlytics
}

@Module
class CrashesBootstrapModule {
    @Provides
    @BootScope
    fun provideFabric(
        @BootScope app: AndroidApplication,
        @Named("fabric") init: FabricInit?
    ): Fabric {
        val builder = Fabric.Builder(app)
        val fabric = init?.run { invoke(builder) }
                     ?: builder.build()
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
