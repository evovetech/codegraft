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

import java.util.Locale
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic.Kind.ERROR
import javax.tools.Diagnostic.Kind.NOTE
import kotlin.reflect.KClass

typealias Env = ProcessingEnv

open
class BaseProcessingEnv(
    override val processorType: KClass<out Processor>,
    override val parent: ProcessingEnvironment
) : ProcessingEnv {
    constructor(
        env: Env
    ) : this(env.processorType, env.parent)

    constructor(
        processor: Processor,
        env: ProcessingEnvironment
    ) : this(processor::class, env)
}

interface ProcessingEnv {
    val processorType: KClass<out Processor>
    val parent: ProcessingEnvironment
    val options: Options
        get() = Options(parent.options)
    val messager: Messager
        get() = parent.messager
    val filer: Filer
        get() = parent.filer
    val elementUtils: Elements
        get() = parent.elementUtils
    val typeUtils: Types
        get() = parent.typeUtils
    val sourceVersion: SourceVersion
        get() = parent.sourceVersion
    val locale: Locale
        get() = parent.locale

    fun processingEnv() = parent
    fun messager() = messager
    fun elements() = elementUtils
    fun types() = typeUtils
    fun filer() = filer

    fun log(element: Element, message: String, vararg args: Any) =
        Unit//  messager.log(element, message, args)

    fun log(message: String, vararg args: Any) =
        Unit//messager.log(message, args)

    fun error(element: Element, message: String, vararg args: Any) =
        Unit// messager.error(element, message, args)

    fun error(message: String, vararg args: Any) =
       Unit// messager.error(message, args)

    companion object {
        const val ALL_ANNOTATIONS = "*"
        val ALL_ANNOTATION_TYPES = setOf(ALL_ANNOTATIONS)
    }

    interface Option {
        val key: String
        val defaultValue: String

        fun get(options: Map<String, String>): String {
            return options[key] ?: defaultValue
        }
    }

    open
    class Options(
        private val provided: Map<String, String>
    ) {
        operator
        fun get(option: Option): String =
            option.get(provided)

        override
        fun toString(): String {
            return "Options(provided=$provided)"
        }
    }
}

fun newEnv(
    processor: Processor,
    processingEnvironment: ProcessingEnvironment
): ProcessingEnv = BaseProcessingEnv(processor, processingEnvironment)

fun Messager.log(element: Element, message: String, vararg args: Any) {
    val msg = msg(message, args)
    printMessage(NOTE, msg, element)
}

fun Messager.log(message: String, vararg args: Any) {
    val msg = msg(message, args)
    printMessage(NOTE, msg)
}

fun Messager.error(element: Element, message: String, vararg args: Any) {
    val msg = msg(message, args)
    printMessage(ERROR, msg, element)
}

fun Messager.error(message: String, vararg args: Any) {
    val msg = msg(message, args)
    printMessage(ERROR, msg)
}

private
fun msg(message: String, args: Array<out Any>) = if (args.isEmpty()) {
    message
} else {
    String.format(message, *args)
}
