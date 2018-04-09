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
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element

/**
 * Created by layne on 2/27/18.
 */

abstract
class BaseProcessor : BasicAnnotationProcessor() {
    private
    val env: Env by lazy { Env(this) }

    abstract
    fun processSteps(): List<ProcessStep>

    final override
    fun initSteps(): MutableIterable<ProcessingStep> = processSteps()
            .map { Step(env, it) }
            .toMutableList()

    override
    fun getSupportedSourceVersion() =
        SourceVersion.latestSupported()!!

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
                .mapNotNull(::mapOutput)
                .toSet()

        private
        fun mapOutput(
            output: Output
        ): Element? = when (output) {
            is DeferredOutput -> output.element
            is NoOutput -> null
            is BaseOutput -> {
                output.writeTo(env.filer())
                null
            }
        }
    }

    private class Env(
        parent: BaseProcessor
    ) : sourcerer.Env(
        parent.processingEnv!!
    ) {
        override
        val processorType = parent::class
    }
}
