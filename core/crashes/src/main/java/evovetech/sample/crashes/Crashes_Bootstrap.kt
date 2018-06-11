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

import io.fabric.sdk.android.Fabric
import sourcerer.inject.Bootstrap.Component
import sourcerer.inject.Bootstrap.Module
import sourcerer.inject.Bootstrap.Provides
import sourcerer.inject.Bootstrap.Singleton

@Component(
    modules = [CrashesBootstrapModule2::class]
)
interface CrashesBootstrapComponent {
    val fabric: Fabric
}

@Module(
    applicationComponents = [CrashesComponent2::class]
)
class CrashesBootstrapModule2 {
    @Provides
    @Singleton
    fun provideFabric(
        app: AndroidApplication,
        init: FabricInit?
    ): Fabric {
        val builder = Fabric.Builder(app)
        val fabric = init?.let { it(builder) } ?: builder.build()
        return Fabric.with(fabric)
    }
}
