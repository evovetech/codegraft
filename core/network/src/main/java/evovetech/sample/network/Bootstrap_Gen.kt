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

package evovetech.sample.network

import android.app.Application
import dagger.Component
import dagger.Module
import dagger.Provides
import evovetech.sample.crashes.Crashes
import evovetech.sample.crashes.CrashesBootstrapModule
import evovetech.sample.crashes.CrashesComponent_ApplicationComponent
import evovetech.sample.crashes.CrashesComponent_BootstrapBuilder
import evovetech.sample.crashes.CrashesComponent_BootstrapModule
import io.fabric.sdk.android.Fabric
import sourcerer.inject.ApplicationComponent
import sourcerer.inject.BootScope
import javax.inject.Singleton

//
// Generated
//

// individual component
// module generated
@ApplicationComponent(
    // TODO: maybe includes instead of dependencies
    dependencies = [CrashesComponent_ApplicationComponent::class],
    modules = [ClientPlugin::class]
)
interface ClientComponent_ApplicationComponent : ClientComponent

// package component
// application generated
@Singleton
@Component(modules = [ClientPlugin::class, Crashes::class])
interface AppComponent :
    ClientComponent_ApplicationComponent,
    CrashesComponent_ApplicationComponent {

    @Component.Builder
    interface Builder :
        CrashesComponent_ApplicationComponent.Builder {

        fun build(): AppComponent
    }
}

@Module(
    includes = [
        CrashesComponent_BootstrapModule::class
    ]
)
class BootModule {
    @Provides
    @BootScope
    fun provideComponent(
        app: Application,
        fabric: Fabric
    ): AppComponent = DaggerAppComponent.builder().run {
        application(app)
        fabric(fabric)
        build()
    }
}

@BootScope
@Component(modules = [BootModule::class])
interface BootComponent {
    val component: AppComponent

    @Component.Builder
    interface Builder :
        CrashesComponent_BootstrapBuilder {
        fun build(): BootComponent
    }
}

class Bootstrap(
    private val delegate: BootComponent.Builder = DaggerBootComponent.builder()
) : BootComponent.Builder by delegate {
    init {
        // defaults
        delegate.fabric(CrashesBootstrapModule::buildFabric)
    }

    override
    fun application(app: Application) {
        delegate.application(app)
    }

    override
    fun fabric(init: (Fabric.Builder) -> Fabric) {
        delegate.fabric(init)
    }

    override
    fun build(): BootComponent {
        return delegate.build()
    }

    inline
    fun build(init: BootComponent.Builder.() -> Unit): BootComponent {
        this.init()
        return build()
    }

    companion object {
        @JvmStatic
        inline
        fun create(
            init: BootComponent.Builder.() -> Unit
        ): BootComponent = Bootstrap()
                .build(init)
    }
}
