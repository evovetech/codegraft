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
        env.compareAndSet(null, createEnv(newEnv(this, processingEnvironment)))
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
