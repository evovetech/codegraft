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

import dagger.Module
import dagger.Provides
import io.fabric.sdk.android.Fabric
import sourcerer.inject.BootScope
import sourcerer.inject.Bootstrap
import sourcerer.inject.android.AndroidApplication
import javax.inject.Named

@Module
@Bootstrap.Module(
    applicationComponent = CrashesComponent2::class
)
class CrashesBootstrapModule2 {
    @Provides
    @BootScope
    fun provideFabric(
        app: AndroidApplication,
        @Named("fabricInit") fabricInit: FabricInit?
    ): Fabric {
        val builder = Fabric.Builder(app)
        val fabric = fabricInit?.let { it(builder) } ?: builder.build()
        return Fabric.with(fabric)
    }
}
