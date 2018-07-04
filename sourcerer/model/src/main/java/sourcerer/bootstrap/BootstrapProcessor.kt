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

import dagger.BindsInstance
import sourcerer.BaseProcessor
import sourcerer.Output
import sourcerer.ProcessStep
import sourcerer.processor.Env
import sourcerer.processor.ProcessingEnv
import sourcerer.processor.ProcessingEnv.Options
import sourcerer.toProcessStep
import javax.annotation.processing.RoundEnvironment
import javax.inject.Inject
import javax.inject.Singleton
import javax.lang.model.util.Types

class BootstrapProcessor(
    val isApplication: Boolean
) : BaseProcessor() {

    @Inject lateinit var types: Types
    @Inject lateinit var options: Options

    @Inject lateinit
    var androidInjectStep: AndroidInjectStep

    @Inject lateinit
    var componentStep: ComponentStep

    private
    var processed = false

    val processingEnv: ProcessingEnv
        get() = env

    fun isProcessed(): Boolean {
        return processed
    }

    override
    fun processSteps(): List<ProcessStep> {
        val component: Component = DaggerBootstrapProcessor_Component.builder().run {
            env(env)
            build()
        }
        component.inject(this)
        return component.processSteps
                .map(AnnotationStep::toProcessStep)
                .toList()
    }

    override
    fun postRound(roundEnv: RoundEnvironment) {
        env.log(
            "postRound($roundEnv) " +
            "{ " +
            "isApplication=$isApplication, " +
            "processingOver=${roundEnv.processingOver()}, " +
            "processed=$processed, " +
            "componentStep.processed=${componentStep.processed}" +
            " }"
        )
        if (isApplication
            && !roundEnv.processingOver()
            && !processed
            && componentStep.processed) {
            val outputs = componentStep.run {
                //                env.postProcess()
                emptyList<Output>()
            }
            env.log("postRound: outputs=$outputs")
            postProcess(outputs)
            processed = true
        } else {
            env.log("postRound: no outputs")
        }
    }

    @Singleton
    @dagger.Component(modules = [AnnotationStepsModule::class])
    interface Component {
        fun inject(processor: BootstrapProcessor)
        val processSteps: Set<AnnotationStep>

        @dagger.Component.Builder
        interface Builder {
            @BindsInstance fun env(env: Env)
            fun build(): Component
        }
    }
}
