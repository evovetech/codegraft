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

import com.google.auto.common.BasicAnnotationProcessor
import com.google.common.collect.ImmutableList
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.multibindings.IntoSet
import sourcerer.processor.ProcessingEnv
import sourcerer.processor.ProcessingEnv.Option
import sourcerer.processor.newEnv
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

typealias ExactSet<T> = Set<@JvmSuppressWildcards T>

open
class RoundProcessor2(
    private val isApplication: Boolean
) : DelegatingProcessor() {
    private lateinit
    var data: ProcessData

    @Inject
    fun inject(data: ProcessData) {
        this.data = data
    }

    private
    var processed = false

    fun isProcessed(): Boolean {
        return processed
    }

    val env: ProcessingEnv
        get() = data.env

    val processingEnv: ProcessingEnv
        get() = data.env

    val processors: ImmutableList<Processor>
        get() = data.processors

    val steps: RoundSteps
        get() = data.steps

    val options: ProcessingEnv.Options
        get() = data.env.options

    private
    var round: ParentRound = ParentRound()

    override
    fun initProcessors(
        env: ProcessingEnvironment
    ): List<Processor> {
        val component = DaggerRoundProcessor2Component.builder().run {
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

        // TODO:
        val outputs = round.outputs
                .flatMap(Round::outputs)
        env.log("outputs = $outputs")
        this.processed = outputs.isEmpty()

        this.round = round
        return false
    }
}

class ProcessStepsDelegate
@Inject constructor(
    private val steps: RoundSteps
) : BasicAnnotationProcessor() {
    override
    fun initSteps(): Iterable<ProcessingStep> {
        return steps
    }

    override
    fun postRound(roundEnv: RoundEnvironment) {
        steps.forEach { step ->
            step.postRound(roundEnv)
        }
    }

    override
    fun getSupportedSourceVersion() =
        SourceVersion.latestSupported()!!

    override
    fun getSupportedOptions(): Set<String> = steps
            .flatMap(RoundStep::supportedOptions)
            .map(Option::key)
            .toSet()
}

@Module(includes = [AnnotationStepsModule::class])
interface RoundProcessorsModule {
    @Binds @IntoSet
    fun bindProcessSteps(processStepsDelegate: ProcessStepsDelegate): Processor
}

@Singleton
@Component(modules = [RoundProcessorsModule::class])
interface RoundProcessor2Component {
    val processData: ProcessData

    fun inject(processor: RoundProcessor2)

    @Component.Builder
    interface Builder {
        @BindsInstance fun env(env: Env)
        @BindsInstance fun isApplication(@Named("isApplication") isApplication: Boolean)
        fun build(): RoundProcessor2Component
    }
}

class RoundProcessor : BasicAnnotationProcessor() {
    private lateinit
    var data: ProcessData

    @Inject
    fun inject(data: ProcessData) {
        this.data = data
    }

    val env: ProcessingEnv by lazy {
        newEnv(processingEnv)
    }
    val steps: RoundSteps
        get() = data.steps

    override
    fun getSupportedSourceVersion() =
        SourceVersion.latestSupported()!!

    override
    fun getSupportedOptions(): Set<String> = steps
            .flatMap(RoundStep::supportedOptions)
            .map(Option::key)
            .toSet()

    override
    fun initSteps(): Iterable<ProcessingStep> {
        return steps
    }

    override
    fun postRound(roundEnv: RoundEnvironment) {
        steps.forEach { step ->
            step.postRound(roundEnv)
        }
    }

//
//    final override
//    fun initSteps(): Iterable<ProcessingStep> = steps
//            .map { Step(env, it) }
//            .toList()
//
//
//    fun postProcess(outputs: List<Output>) {
//        outputs.map {
//            env.mapOutput(it)
//        }
//        @Inject lateinit var types: Types
//        @Inject lateinit var options: Options
//    }
//
//    override
//    fun processSteps(): List<ProcessStep> {
//        val component = DaggerBootstrapProcessor_Component.builder().run {
//            env(env)
//            build()
//        }
//        component.inject(this)
//        return component.processSteps
//                .toList()
//    }
//
//    override
//    fun postRound(roundEnv: RoundEnvironment) {
//        env.log(
//            "postRound($roundEnv) " +
//            "{ " +
//            "isApplication=$isApplication, " +
//            "processingOver=${roundEnv.processingOver()}, " +
//            "processed=$processed, " +
//            "componentStep.processed=${componentStep.processed}" +
//            " }"
//        )
//        if (isApplication
//            && !roundEnv.processingOver()
//            && !processed
//            && componentStep.processed) {
//            val outputs = componentStep.run {
//                env.postProcess()
//            }
//            env.log("postRound: outputs=$outputs")
//            postProcess(outputs)
//            processed = true
//        } else {
//            env.log("postRound: no outputs")
//        }
//    }
//
//    @Singleton
//    @dagger.Component(modules = [AnnotationStepsModule::class])
//    interface Component {
//        fun inject(processor: RoundProcessor)
//        val processSteps: Set<ProcessStep>
//
//        @dagger.Component.Builder
//        interface Builder {
//            @BindsInstance fun env(env: Env)
//            fun build(): Component
//        }
//    }
}
