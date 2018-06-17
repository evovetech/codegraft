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

package sourcerer.dev

import dagger.Module
import dagger.Provides
import sourcerer.google.auto.factory.AutoFactory
import sourcerer.google.auto.factory.Provided
import sourcerer.processor.Env
import sourcerer.processor.Env.Options
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

@Module
class EnvModule {
    @Provides
    fun providesTypes(processingEnv: ProcessingEnvironment): Types {
        return processingEnv.typeUtils
    }

    @Provides
    fun providesElements(processingEnv: ProcessingEnvironment): Elements {
        return processingEnv.elementUtils
    }

    @Provides
    fun providesEnv(processingEnv: ProcessingEnvironment): Env {
        return Env(processingEnv)
    }

    @Provides
    fun providesOptions(processingEnv: ProcessingEnvironment): Options {
        return Options(processingEnv.options)
    }
}

@Module
class SampleModule
@AutoFactory constructor(
    @Provided val processingEnv: ProcessingEnvironment
) {
    @Provides
    fun providesTypes(): Types {
        return processingEnv.typeUtils
    }
}
