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
import sourcerer.inject.FunctionQualifier
import javax.inject.Inject
import javax.inject.Singleton

//
// Generated
//

// individual component
@Singleton
@Component(modules = [Crashes::class])
interface CrashesComponent_BootstrapComponent : CrashesComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance fun application(app: Application)
        @BindsInstance fun fabric(fabric: Fabric)
        fun build(): CrashesComponent_BootstrapComponent
    }
}

// group component
@BootScope
class LibraryComponent
@Inject constructor(
    val crashesComponent: CrashesComponent_BootstrapComponent
) : CrashesComponent_BootstrapComponent by crashesComponent

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
        val builder = fabricBuilder(app)
        val fabric = init(builder)
        return initializeFabric(fabric)
    }

    @Provides
    @BootScope
    fun provideCrashesComponent(
        app: Application,
        fabric: Fabric
    ): CrashesComponent_BootstrapComponent {
        return DaggerCrashesComponent_BootstrapComponent.builder().run {
            application(app)
            fabric(fabric)
            build()
        }
    }
}

@BootScope
@Component(modules = [BootMod::class])
interface BootComp {
    val component: LibraryComponent

    @Component.Builder
    interface Builder {
        @BindsInstance fun application(app: Application): Builder
        @BindsInstance fun fabric(
            @FunctionQualifier(
                params = [Fabric.Builder::class],
                returnType = [Fabric::class]
            ) init: (Fabric.Builder) -> Fabric
        ): Builder

        fun build(): BootComp
    }
}