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
import sourcerer.inject.LibComponent
import sourcerer.inject.LibModule
import javax.inject.Singleton

@LibModule(includes = [Crashes::class])
@LibComponent(modules = [Crashes::class])
interface CrashesComponent {
    val fabric: Fabric
    val crashlytics: Crashlytics
}

@Module
class Crashes {
    @Provides
    @Singleton
    fun provideFabric(app: Application): Fabric {
        return Fabric.with(app)
    }

    @Provides
    @Singleton
    fun provideCrashlytics(fabric: Fabric): Crashlytics {
        return Crashlytics.getInstance()
    }
}
