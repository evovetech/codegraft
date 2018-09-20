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
