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

import java.util.Locale
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.lang.model.SourceVersion
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic.Kind
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

    private val _messager = super.messager

    override
    val messager: Messager = object : Messager {
        override
        fun printMessage(kind: Kind?, msg: CharSequence?) {
            // TODO: Log level
            _messager.printMessage(kind, msg)
        }

        override
        fun printMessage(kind: Kind?, msg: CharSequence?, element: Element?) {
            // TODO: Log level
            _messager.printMessage(kind, msg, element)

        }

        override
        fun printMessage(kind: Kind?, msg: CharSequence?, element: Element?, annotationMirror: AnnotationMirror) {
            // TODO: Log level
//            _messager.printMessage(kind, msg, element, annotationMirror)
        }

        override
        fun printMessage(
            kind: Kind?,
            msg: CharSequence?,
            element: Element?,
            annotationMirror: AnnotationMirror?,
            annotationValue: AnnotationValue?
        ) {
            // TODO: Log level
//            _messager.printMessage(kind, msg, element, annotationMirror, annotationValue)
        }
    }
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
        messager.log(element, message, args)

    fun log(message: String, vararg args: Any) =
        messager.log(message, args)

    fun error(element: Element, message: String, vararg args: Any) =
        messager.error(element, message, args)

    fun error(message: String, vararg args: Any) =
        messager.error(message, args)

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

// TODO: allow variance
const val DEBUG = false

fun Processor.newEnv(
    processingEnvironment: ProcessingEnvironment
): ProcessingEnv = BaseProcessingEnv(this, processingEnvironment)

fun Messager.log(element: Element, message: String, vararg args: Any) {
    if (DEBUG) {
        val msg = msg(message, args)
        printMessage(NOTE, msg, element)
    }
}

fun Messager.log(message: String, vararg args: Any) {
    if (DEBUG) {
        val msg = msg(message, args)
        printMessage(NOTE, msg)
    }
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
