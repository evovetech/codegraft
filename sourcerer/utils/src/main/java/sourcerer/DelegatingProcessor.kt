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
