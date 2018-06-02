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
import javax.inject.Singleton

//
// Generated
//

// individual component
interface CrashesComponent_BootstrapComponent : CrashesComponent {
    interface Builder {
        @BindsInstance fun application(app: Application)
        @BindsInstance fun fabric(fabric: Fabric)
    }
}

// group component
interface LibraryComponent : CrashesComponent_BootstrapComponent {
    interface Builder : CrashesComponent_BootstrapComponent.Builder
}

@Singleton
@Component(modules = [Crashes::class])
interface LibraryComponentImpl : LibraryComponent {
    @Component.Builder
    interface Builder : LibraryComponent.Builder {
        fun build(): LibraryComponent
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
        val builder = fabricBuilder(app)
        val fabric = init(builder)
        return initializeFabric(fabric)
    }

    @Provides
    @BootScope
    fun provideCrashesComponent(
        app: Application,
        fabric: Fabric
    ): LibraryComponent {
        return DaggerLibraryComponentImpl.builder().run {
            application(app)
            fabric(fabric)
            build()
        }
    }
}

interface BootComp {
    val component: LibraryComponent

    interface Builder {
        @BindsInstance fun application(app: Application)
        @BindsInstance fun fabric(
            @FunctionQualifier(
                params = [Fabric.Builder::class],
                returnType = [Fabric::class]
            ) init: (Fabric.Builder) -> Fabric
        )
    }
}

@BootScope
@Component(modules = [BootMod::class])
interface BootCompImpl : BootComp {
    @Component.Builder
    interface Builder : BootComp.Builder {
        fun build(): BootCompImpl
    }
}
