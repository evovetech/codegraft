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
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import io.fabric.sdk.android.Fabric
import sourcerer.inject.BootScope
import javax.inject.Singleton

//
// Generated
//

@Singleton
@Component(modules = [Crashes::class])
interface CrashesComponentImpl : CrashesComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance fun app(app: Application): Builder
        @BindsInstance fun fabric(fabric: Fabric): Builder
        fun build(): CrashesComponentImpl
    }
}

@Module
class BootMod {
    @Provides
    @BootScope
    fun provideFabric(
        app: Application,
        @FunctionQualifier(
            params = [Fabric.Builder::class],
            returnType = [Fabric::class]
        ) init: (Fabric.Builder) -> Fabric
    ): Fabric {
        val builder = provideFabricBuilder(app)
        val fabric = init(builder)
        return initializeFabric(fabric)
    }

    @Provides
    @BootScope
    fun provideCrashesComponent(
        app: Application,
        fabric: Fabric
    ): CrashesComponent {
        return DaggerCrashesComponentImpl.builder()
                .app(app)
                .fabric(fabric)
                .build()
    }
}

@BootScope
@Component(modules = [BootMod::class])
interface BootComp {
    val component: CrashesComponent

    @Component.Builder
    interface Builder {
        @BindsInstance fun app(app: Application): Builder
        @BindsInstance fun buildFabric(
            @FunctionQualifier(
                params = [Fabric.Builder::class],
                returnType = [Fabric::class]
            ) init: (Fabric.Builder) -> Fabric
        ): Builder

        fun build(): BootComp
    }
}
