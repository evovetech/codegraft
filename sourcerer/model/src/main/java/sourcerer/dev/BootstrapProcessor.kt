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

import com.google.auto.service.AutoService
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import sourcerer.BaseProcessor
import sourcerer.ProcessStep
import sourcerer.processor.Env
import sourcerer.processor.Env.Options
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.inject.Inject
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

@AutoService(Processor::class)
class BootstrapProcessor : BaseProcessor() {
    @Inject lateinit var types: Types
    @Inject lateinit var options: Options

    override
    fun processSteps(): List<ProcessStep> {
        val component = DaggerBootstrapProcessorComponent.builder().run {
            processingEnv(processingEnv)
            build()
        }
        component.inject(this)
        return component.processSteps
    }
}

@Module
class BootstrapProcessStepsModule {
    @Provides
    fun provideProcessSteps(
        buildsStep: BuildsStep,
        bootstrapModuleStep: BootstrapModuleStep,
        bootstrapComponentStep: BootstrapComponentStep
    ): List<ProcessStep> = listOf(
        buildsStep,
        bootstrapModuleStep,
        bootstrapComponentStep
    )

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

@Component(modules = [BootstrapProcessStepsModule::class])
interface BootstrapProcessorComponent {
    fun inject(processor: BootstrapProcessor)
    val processSteps: List<ProcessStep>

    @Component.Builder
    interface Builder {
        @BindsInstance fun processingEnv(processingEnv: ProcessingEnvironment)
        fun build(): BootstrapProcessorComponent
    }
}
