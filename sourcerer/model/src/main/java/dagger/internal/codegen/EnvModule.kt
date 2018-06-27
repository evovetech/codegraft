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

package dagger.internal.codegen

import dagger.Binds
import dagger.Module
import dagger.Provides
import sourcerer.Env
import sourcerer.processor.Env.Options
import sourcerer.processor.ProcessingEnv
import javax.annotation.processing.Filer
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

@Module(includes = [EnvModule2::class])
class EnvModule {
    @Provides
    fun providesTypes(env: Env): Types {
        return env.typeUtils
    }

    @Provides
    fun providesElements(env: Env): Elements {
        return env.elementUtils
    }

    @Provides
    fun providesOptions(env: Env): Options {
        return env.options
    }

    @Provides
    fun providesFiler(env: Env): Filer {
        return env.filer
    }
}

@Module
interface EnvModule2 {
    @Binds fun bindsEnv(env: sourcerer.Env): sourcerer.processor.Env
    @Binds fun bindsEnv2(env: sourcerer.processor.Env): ProcessingEnv
}
