/*
 * Copyright (C) 2018 evove.tech
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package codegraft

import codegraft.ProcessStepsProcessor.Component.Builder
import codegraft.bootstrap.AppComponentStep
import codegraft.bootstrap.Env
import com.google.common.collect.ImmutableList
import dagger.BindsInstance
import sourcerer.DelegatingProcessor
import sourcerer.Output
import sourcerer.ParentRound
import sourcerer.mapOutput
import sourcerer.processor.ProcessingEnv
import sourcerer.processor.ProcessingEnv.Options
import sourcerer.processor.newEnv
import sourcerer.toImmutableList
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import javax.lang.model.element.TypeElement

open
class ProcessStepsProcessor(
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

    val currentRound: ParentRound
        get() = round

    override
    fun initProcessors(
        env: ProcessingEnvironment
    ): List<Processor> {
        val builder: Builder =
            DaggerProcessStepsProcessor_Component.builder()
        val component = builder.run {
            env(newEnv(env))
            isApplication(isApplication)
            build()
        }
        component.inject(this)
        return data.processors
    }

    override
    fun process(elements: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val round = this.round.process(elements, roundEnv, steps) {
            super.process(elements, roundEnv)
        }

        this.round = round.postRound(roundEnv)
        return false
    }

    fun ParentRound.postRound(roundEnv: RoundEnvironment): ParentRound {
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
            return copy(parentOutputs = outputs.toImmutableList())
        } else {
            env.log("postRound: no outputs")
            return this
        }
    }

    private
    fun ProcessData.finish(): List<Output> =
        if (isApplication) {
            val generatedPluginModules = generatePluginBindingsStep.generatedModules
            val storedPluginModules = generatePluginBindingsStep.storedModules
            val generatedModules = androidInjectStep.generatedModules
            val storedModules = androidInjectStep.storedModules()
            val generatedComponents = bootstrapComponentStep.generatedComponents
            val storedComponents = bootstrapComponentStep.storedComponents()

            env.log("storedComponents = $storedComponents")
            val appComponent = appComponentStep.process(
                generatedPluginModules = generatedPluginModules,
                storedPluginModules = storedPluginModules,
                generatedModules = generatedModules,
                storedModules = storedModules,
                generatedComponents = generatedComponents,
                storedComponents = storedComponents
            )
            appComponent.flatMap(AppComponentStep.Output::outputs)
        } else {
            // just sourcerer things
            listOf(
                bootstrapComponentStep.sourcererOutput(),
                androidInjectStep.sourcererOutput(),
                generatePluginBindingsStep.sourcererOutput()
            )
        }

    @Singleton
    @dagger.Component(modules = [RoundProcessorsModule::class])
    interface Component {
        val processData: ProcessData

        fun inject(processor: ProcessStepsProcessor)

        @dagger.Component.Builder
        interface Builder {
            @BindsInstance fun env(env: Env)
            @BindsInstance fun isApplication(@Named("isApplication") isApplication: Boolean)
            fun build(): Component
        }
    }
}
