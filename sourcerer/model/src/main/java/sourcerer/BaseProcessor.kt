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

package sourcerer

import com.google.auto.common.BasicAnnotationProcessor
import sourcerer.processor.Env.Option
import sourcerer.processor.ProcessingEnv
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element

/**
 * Created by layne on 2/27/18.
 */

abstract
class BaseProcessor : BasicAnnotationProcessor() {
    protected
    val env: Env by lazy {
        Env(this)
    }
    private
    val steps by lazy {
        processSteps()
    }

    abstract
    fun processSteps(): List<ProcessStep>

    private
    fun supportedOptions(): Iterable<Option> = steps
            .flatMap { it.supportedOptions() }

    final override
    fun initSteps(): MutableIterable<ProcessingStep> = steps
            .map { Step(env, it) }
            .toMutableList()

    override
    fun getSupportedSourceVersion() =
        SourceVersion.latestSupported()!!

    final override
    fun getSupportedOptions(): Set<String> = supportedOptions()
            .map(Option::key)
            .toSet()

    fun postProcess(outputs: List<Output>) {
        outputs.map {
            env.mapOutput(it)
        }
    }

    private
    class Step(
        val env: Env,
        val step: ProcessStep
    ) : ProcessingStep {
        override
        fun annotations() = step.annotations()
                .map { it.java }
                .toSet()

        override
        fun process(annotationElements: AnnotationElements) =
            step.process(annotationElements)

        private
        fun ProcessStep.annotations() =
            env.annotations()

        private
        fun ProcessStep.process(
            annotationElements: AnnotationElements
        ) = env.process(annotationElements)
                .flatMap { it.value }
                .onEach { env.log("output=$it") }
                .mapNotNull(env::mapOutput)
                .toSet()
    }

    protected
    class Env(
        parent: BaseProcessor
    ) : sourcerer.Env(
        parent.processingEnv!!
    ) {
        override
        val processorType = parent::class
    }
}

fun ProcessingEnv.mapOutput(
    output: Output
): Element? = when (output) {
    is DeferredOutput -> output.element
    is NoOutput -> null
    is BaseOutput -> {
        output.writeTo(filer)
        null
    }
}
