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
import evovetech.sample.crashes.CrashesComponent.Builder.fabricBuilder
import evovetech.sample.crashes.CrashesComponent.Builder.initializeFabric
import io.fabric.sdk.android.Fabric
import sourcerer.inject.ApplicationComponent
import sourcerer.inject.BootScope
import sourcerer.inject.FunctionQualifier
import javax.inject.Singleton

//
// Generated
//

// individual component
// module generated
@ApplicationComponent(modules = [Crashes::class])
interface CrashesComponent_ApplicationComponent : CrashesComponent {
    @ApplicationComponent.Builder
    interface Builder {
        @BindsInstance fun application(app: Application)
        @BindsInstance fun fabric(fabric: Fabric)
    }
}

@Module
class CrashesComponent_BootstrapModule {
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
}

// TODO: annotate
interface CrashesComponent_BootstrapBuilder {
    @BindsInstance fun application(app: Application)
    @BindsInstance fun fabric(
        @FunctionQualifier(
            params = [Fabric.Builder::class],
            returnType = [Fabric::class]
        ) init: (Fabric.Builder) -> Fabric
    )
}

// package component
// application generated
@Singleton
@Component(modules = [Crashes::class])
interface AppComponent : CrashesComponent_ApplicationComponent {
    @Component.Builder
    interface Builder : CrashesComponent_ApplicationComponent.Builder {
        fun build(): AppComponent
    }
}

@Module(includes = [CrashesComponent_BootstrapModule::class])
class BootModule {
    @Provides
    @BootScope
    fun provideComponent(
        app: Application,
        fabric: Fabric
    ): AppComponent {
        return DaggerAppComponent.builder().run {
            application(app)
            fabric(fabric)
            build()
        }
    }
}

@BootScope
@Component(modules = [BootModule::class])
interface BootComponent {
    val component: AppComponent

    @Component.Builder
    interface Builder : CrashesComponent_BootstrapBuilder {
        fun build(): BootComponent
    }
}
