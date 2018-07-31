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

package codegraft.inject.extension.crashlytics

import codegraft.inject.BootScope
import codegraft.inject.android.AndroidApplication
import dagger.Module
import dagger.Provides
import io.fabric.sdk.android.Fabric
import io.fabric.sdk.android.Fabric.Builder
import javax.inject.Named

typealias FabricInit = Fabric.Builder.() -> Fabric

@Module
class CrashesBootstrapModule {
    @Provides
    @BootScope
    fun provideFabric(
        @BootScope app: AndroidApplication,
        @Named("fabric") init: FabricInit?
    ): Fabric {
        val builder = Builder(app)
        val fabric = init?.run { invoke(builder) }
                     ?: builder.build()
        return Fabric.with(fabric)
    }
}
