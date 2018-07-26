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
import sourcerer.processor.Env
import sourcerer.processor.ProcessingEnv
import sourcerer.processor.ProcessingEnv.Option
import sourcerer.processor.newEnv
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element

/**
 * Created by layne on 2/27/18.
 */

abstract
class BaseProcessor : BasicAnnotationProcessor() {
    protected
    val env: Env by lazy {
        newEnv(processingEnv)
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
    fun initSteps(): Iterable<ProcessingStep> = steps
            .map { Step(env, it) }
            .toList()

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
                .mapNotNull(env::mapOutput)
                .toSet()
    }
}

fun ProcessingEnv.mapOutput(
    output: Output
): Element? = when (output) {

    is DeferredOutput -> {
        log("DeferredOutput=${output.element}")
        output.element
    }

    is NoOutput -> {
        log("NoOutput")
        null
    }

    is BaseOutput -> {
        output.writeTo(filer)

        when (output) {
            is JavaOutput -> {
                log("JavaOutput=${output.outKlass.rawType}")
            }
            is SourcererOutput -> {
                log("SourcererOutput=${output.file()}")
            }
        }

        null
    }
}
