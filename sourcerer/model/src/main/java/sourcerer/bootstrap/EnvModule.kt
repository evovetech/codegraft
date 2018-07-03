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

package sourcerer.bootstrap

import dagger.Module
import dagger.Provides
import sourcerer.processor.ProcessingEnv
import sourcerer.processor.ProcessingEnv.Options
import sourcerer.processor.log
import javax.annotation.processing.Filer
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

@Module
class EnvModule {
    @Provides
    fun providesTypes(env: ProcessingEnv): Types {
        return env.typeUtils
    }

    @Provides
    fun providesElements(env: ProcessingEnv): Elements {
        return env.elementUtils
    }

    @Provides
    fun providesOptions(env: ProcessingEnv): Options {
        return env.options.apply {
            env.parent.messager.log("options = $this")
        }
    }

    @Provides
    fun providesFiler(env: ProcessingEnv): Filer {
        return env.filer
    }
}
