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

import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic.Kind.ERROR
import javax.tools.Diagnostic.Kind.NOTE

open
class Env(
    processingEnv: ProcessingEnvironment
) : ProcessingEnvironment by processingEnv {
    fun processingEnv(): ProcessingEnvironment {
        return this
    }

    fun messager(): Messager {
        return messager
    }

    fun elements(): Elements {
        return elementUtils
    }

    fun types(): Types {
        return typeUtils
    }

    fun filer(): Filer {
        return filer
    }

    fun log(element: Element, message: String, vararg args: Any) {
        val msg = msg(message, args)
        messager().printMessage(NOTE, msg, element)
    }

    fun log(message: String, vararg args: Any) {
        val msg = msg(message, args)
        messager().printMessage(NOTE, msg)
    }

    fun error(element: Element, message: String, vararg args: Any) {
        val msg = msg(message, args)
        messager().printMessage(ERROR, msg, element)
    }

    fun error(message: String, vararg args: Any) {
        val msg = msg(message, args)
        messager().printMessage(ERROR, msg)
    }

    private
    fun msg(message: String, args: Array<out Any>) = if (args.isEmpty()) {
        message
    } else {
        String.format(message, *args)
    }

    companion object {
        const val ALL_ANNOTATIONS = "*"
        val ALL_ANNOTATION_TYPES = setOf(ALL_ANNOTATIONS)
    }
}
