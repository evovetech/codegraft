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

@file:Bootstrap(modules = [Crashes::class])
@file:JvmName("BootstrapCrashes")

package evovetech.sample.crashes

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import io.fabric.sdk.android.Fabric
import sourcerer.inject.Bootstrap
import sourcerer.inject.Builds
import sourcerer.inject.Initializes
import javax.inject.Singleton

@Builds(Fabric::class)
fun provideFabricBuilder(app: Application): Fabric.Builder {
    return Fabric.Builder(app)
}

@Initializes
fun initializeFabric(fabric: Fabric): Fabric {
    return Fabric.with(fabric)
}

//
// Generated
//

@Singleton
@Component(modules = [Crashes::class])
interface FabricComponent : CrashesComponent {
    val fabric: Fabric

    @Component.Builder
    abstract
    class Builder {
        @BindsInstance abstract
        fun app(app: Application): Builder

        @BindsInstance abstract
        fun fabric(fabric: Fabric): Builder

        abstract
        fun build(): FabricComponent
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
    fun provideFabricComponent(fabric: Fabric): FabricComponent {
        return DaggerFabricComponent.builder()
                .fabric(fabric)
                .build()
    }
}

@BootScope
@Component(modules = [BootMod::class])
interface BootComp {
    val fabric: FabricComponent

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
