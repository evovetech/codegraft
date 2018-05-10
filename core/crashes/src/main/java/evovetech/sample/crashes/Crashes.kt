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
import dagger.BindsInstance
import dagger.Module
import dagger.Provides
import io.fabric.sdk.android.Fabric
import sourcerer.inject.LibComponent
import sourcerer.inject.LibModule
import javax.inject.Singleton

@LibModule(includes = [Crashes::class])
@LibComponent(modules = [Crashes::class])
interface CrashesComponent {
    val crashes: Crashlytics

    @LibComponent.Builder
    interface Builder {
        @BindsInstance
        fun crashes(
            builder: CrashBuilder
        ): Builder
    }
}

@Module
class Crashes {
    @Provides
    @Singleton
    fun provideFabric(app: Application, builder: CrashBuilder): Fabric {
        val fabric = builder.run {
            Fabric.Builder(app).run {
                init()
                build()
            }
        }
        return Fabric.with(fabric)
    }

    @Provides
    @Singleton
    fun provideCrashlytics(fabric: Fabric): Crashlytics {
        return Crashlytics.getInstance()
    }
}

interface CrashBuilder {
    fun Fabric.Builder.init()
}

typealias CrashBuilderFunc = Fabric.Builder.() -> Unit

fun CrashBuilderFunc.toBuilder() = object : CrashBuilder {
    override
    fun Fabric.Builder.init() = this@toBuilder.invoke(this)
}

fun <B : CrashesComponent.Builder> B.crashes(
    init: CrashBuilderFunc
): B {
    crashes(init.toBuilder())
    return this
}
