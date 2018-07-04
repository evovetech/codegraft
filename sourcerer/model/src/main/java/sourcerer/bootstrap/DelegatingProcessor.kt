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
import javax.annotation.processing.Completion
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

abstract
class DelegatingProcessor : Processor {
    private lateinit
    var processingEnv: ProcessingEnvironment

    private
    val processors: ImmutableList<Processor> by lazy {
        initProcessors(processingEnv)
                .toImmutableList()
    }

    abstract
    fun initProcessors(
        env: ProcessingEnvironment
    ): List<Processor>

    override
    fun process(elements: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        processors.forEach { processor ->
            processor.process(elements, roundEnv)
        }
        return false
    }

    final override
    fun init(env: ProcessingEnvironment): Unit = synchronized(this) {
        processingEnv = env
        processors.forEach { processor ->
            processor.init(env)
        }
    }

    final override
    fun getSupportedOptions(): Set<String> = processors
            .flatMap(Processor::getSupportedOptions)
            .toSet()

    final override
    fun getSupportedSourceVersion(): SourceVersion = processors
            .map(Processor::getSupportedSourceVersion)
            .maxBy(SourceVersion::ordinal)!!

    final override
    fun getSupportedAnnotationTypes(): Set<String> = processors
            .flatMap(Processor::getSupportedAnnotationTypes)
            .toSet()

    override
    fun getCompletions(
        p0: Element?,
        p1: AnnotationMirror?,
        p2: ExecutableElement?,
        p3: String?
    ): Iterable<Completion> = processors.flatMap {
        it.getCompletions(p0, p1, p2, p3)
    }
}
