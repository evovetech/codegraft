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

import com.google.common.collect.ImmutableList
import dagger.BindsInstance
import sourcerer.Output
import sourcerer.mapOutput
import sourcerer.processor.ProcessingEnv
import sourcerer.processor.ProcessingEnv.Options
import sourcerer.processor.newEnv
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import javax.lang.model.element.TypeElement

open
class BootstrapProcessor(
    private val isApplication: Boolean
) : DelegatingProcessor() {
    private lateinit
    var data: ProcessData

    @Inject
    fun inject(
        data: ProcessData
    ) {
        this.data = data
    }

    private
    var finished = false

    fun isFinished(): Boolean {
        return finished
    }

    fun isProcessed(): Boolean {
        return isFinished()
    }

    val env: ProcessingEnv
        get() = data.env

    val processingEnv: ProcessingEnv
        get() = data.env

    val processors: ImmutableList<Processor>
        get() = data.processors

    val steps: RoundSteps
        get() = data.steps

    val options: Options
        get() = data.env.options

    private
    var round: ParentRound = ParentRound()

    override
    fun initProcessors(
        env: ProcessingEnvironment
    ): List<Processor> {
        val component = DaggerBootstrapProcessor_Component.builder().run {
            env(newEnv(env))
            isApplication(isApplication)
            build()
        }
        component.inject(this)
        return data.processors
    }

    override
    fun process(elements: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        env.log("elements = $elements")
        val round = this.round.process(elements, roundEnv, steps) {
            super.process(elements, roundEnv)
        }

        round.postRound(roundEnv)

        this.round = round
        return false
    }

    fun ParentRound.postRound(roundEnv: RoundEnvironment) {
        env.log(
            "postRound($roundEnv) " +
            "{ " +
            "isApplication=$isApplication, " +
            "processingOver=${roundEnv.processingOver()}, " +
            "finished=$finished, " +
            "rounds=$rounds" +
            " }"
        )

        env.log("roundOutputs = $outputs")

        if (!finished
            && !roundEnv.processingOver()
            && deferredElements.isEmpty()
            && outputs.isEmpty()) {

            val outputs = data.finish()
            outputs.map {
                env.mapOutput(it)
            }
            env.log("outputs=$outputs")

            finished = true
        } else {
            env.log("postRound: no outputs")
        }
    }

    private
    fun ProcessData.finish(): List<Output> =
        if (isApplication) {
            val generatedModules = androidInjectStep.modules
            val storedModules = androidInjectStep.storedModules()
            val generatedComponents = bootstrapComponentStep.generatedComponents
            val storedComponents = bootstrapComponentStep.storedComponents()

            env.log("storedComponents = $storedComponents")
            val appComponent = appComponentStep.process(
                generatedModules,
                storedModules,
                generatedComponents,
                storedComponents
            )
            appComponent.flatMap(AppComponentStep.Output::outputs)
        } else {
            // just sourcerer things
            listOf(
                bootstrapComponentStep.sourcererOutput(),
                androidInjectStep.sourcererOutput()
            )
        }

    @Singleton
    @dagger.Component(modules = [RoundProcessorsModule::class])
    interface Component {
        val processData: ProcessData

        fun inject(processor: BootstrapProcessor)

        @dagger.Component.Builder
        interface Builder {
            @BindsInstance fun env(env: Env)
            @BindsInstance fun isApplication(@Named("isApplication") isApplication: Boolean)
            fun build(): Component
        }
    }
}
