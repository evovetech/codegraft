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

import com.google.auto.service.AutoService
import dagger.BindsInstance
import sourcerer.BaseProcessor
import sourcerer.ProcessStep
import sourcerer.processor.Env.Options
import javax.annotation.processing.Processor
import javax.inject.Inject
import javax.lang.model.util.Types

@AutoService(Processor::class)
class BootstrapProcessor : BaseProcessor() {
    @Inject lateinit var types: Types
    @Inject lateinit var options: Options

    override
    fun processSteps(): List<ProcessStep> {
        val component = DaggerBootstrapProcessor_Component.builder().run {
            env(env)
            build()
        }
        component.inject(this)
        return component.processSteps
                .toList()
    }

    @dagger.Component(modules = [BootstrapProcessStepsModule::class])
    interface Component {
        fun inject(processor: BootstrapProcessor)
        val processSteps: Set<ProcessStep>

        @dagger.Component.Builder
        interface Builder {
            @BindsInstance fun env(env: sourcerer.Env)
            fun build(): Component
        }
    }
}