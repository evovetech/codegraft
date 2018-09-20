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

package sourcerer.processor

import com.google.common.base.MoreObjects
import java.util.concurrent.atomic.AtomicReference
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element

abstract
class EnvProcessor<E : Env> : AbstractProcessor() {
    private val env = AtomicReference<E>()

    @Synchronized override
    fun init(processingEnvironment: ProcessingEnvironment) {
        super.init(processingEnvironment)
        env.compareAndSet(null, createEnv(newEnv(processingEnvironment)))
        init(env.get())
    }

    protected abstract
    fun createEnv(env: Env): E

    protected open
    fun init(env: E) {
        // subclass override
    }

    abstract override
    fun getSupportedAnnotationTypes(): Set<String>

    override
    fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    fun env(): E {
        return env.get()!!
    }

    fun log(element: Element, message: String, vararg args: Any) {
        val env = env()
        env.log(element, message, *args)
    }

    fun log(message: String, vararg args: Any) {
        val env = env()
        env.log(message, *args)
    }

    fun error(element: Element, message: String, vararg args: Any) {
        val env = env()
        env.error(element, message, *args)
    }

    fun error(message: String, vararg args: Any) {
        val env = env()
        env.error(message, *args)
    }

    override
    fun toString(): String {
        return MoreObjects.toStringHelper(this)
                .add("supportedAnnotationTypes", supportedAnnotationTypes)
                .toString()
    }

    companion object {
        val ALL_ANNOTATIONS = Env.ALL_ANNOTATIONS
        val ALL_ANNOTATION_TYPES = Env.ALL_ANNOTATION_TYPES
    }
}
