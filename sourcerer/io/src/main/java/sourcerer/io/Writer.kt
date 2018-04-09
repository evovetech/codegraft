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

package sourcerer.io

import com.google.common.collect.ImmutableList
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import okio.BufferedSink
import okio.ByteString
import okio.Okio
import java.io.Closeable
import java.io.Flushable
import java.io.IOException
import java.io.OutputStream
import java.util.UUID
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.element.TypeParameterElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror

class Writer(
    private val sink: BufferedSink
) : Closeable, Flushable {

    @Throws(IOException::class)
    fun write(writeable: Writeable): Writer {
        writeable.writeTo(this)
        return this
    }

    @Throws(IOException::class)
    fun write(`val`: ByteString): Writer {
        val length = `val`.size()
        sink.writeInt(`val`.size())
        log("writeInt: '%d'", length)
        sink.write(`val`)
        log("writeString: '%s'", `val`.utf8())
        return this
    }

    @Throws(IOException::class)
    fun writeString(entry: String): Writer {
        return write(ByteString.encodeUtf8(entry))
    }

    @Throws(IOException::class)
    fun <T : Writeable> writeList(list: List<T>?): Writer {
        val size = list?.size
                   ?: 0
        val uuid = UUID.randomUUID()
        log("writeList(%s) size=%d", uuid, size)
        sink.writeInt(size)
        for (i in 0 until size) {
            val t = list!![i]
            log("writeList(%s) entry=%s", uuid, t)
            t.writeTo(this)
        }
        return this
    }

    fun <T> writeList(
        list: List<T>?,
        inker: (Writer, T) -> Boolean
    ): Writer = writeList(list, object : Inker<T> {
        override
        fun pen(writer: Writer, param: T) = inker(writer, param)
    })

    @Throws(IOException::class)
    fun <T> writeList(
        list: List<T>?,
        inker: Inker<T>
    ): Writer {
        val size = list?.size
                   ?: 0
        val uuid = UUID.randomUUID()
        log("writeList(%s, inker) size=%d", uuid, size)
        sink.writeInt(size)
        for (i in 0 until size) {
            val t = list!![i]
            log("writeList(%s, inker) entry=%s", uuid, t)
            inker.pen(this, list[i])
        }
        return this
    }

    @Throws(IOException::class)
    fun writeStringList(list: List<String>): Writer {
        return writeList(list, STRING_INK)
    }

    @Throws(IOException::class)
    fun writeModifiers(modifiers: Set<Modifier>): Writer {
        return writeList(ImmutableList.copyOf(modifiers), MODIFIER_INK)
    }

    @Throws(IOException::class)
    fun writeTypeParams(typeParams: List<TypeParameterElement>): Writer {
        return writeList(ImmutableList.copyOf(typeParams), TYPE_PARAMETER_INK)
    }

    @Throws(IOException::class)
    fun writeClassName(className: ClassName): Writer {
        // Write package name
        writeString(className.packageName())

        // Write simple names
        writeStringList(className.simpleNames())

        return this
    }

    @Throws(IOException::class)
    fun writeTypeName(typeName: TypeName): Writer {
        write(this, typeName)
        return this
    }

    @Throws(IOException::class)
    fun writeTypeNames(typeNames: List<TypeName>): Writer {
        writeList(this, typeNames)
        return this
    }

    @Throws(IOException::class)
    fun writeParams(params: List<VariableElement>): String {
        val immutableParams = ImmutableList.copyOf(params)

        // write list
        writeList(immutableParams, PARAM_INK)

        // gather and return param names
        var first = true
        val paramString = StringBuilder()
        for (param in immutableParams) {
            val paramName = param.simpleName.toString()
            if (!first) {
                paramString.append(", ")
            }
            first = false
            paramString.append(paramName)
        }
        return paramString.toString()
    }

    @Throws(IOException::class)
    fun writeAnnotations(annotations: List<AnnotationMirror>): Writer {
        writeList(annotations, ANNOTATION_INK)
        return this
    }

    @Throws(IOException::class)
    override fun close() {
        sink.close()
    }

    @Throws(IOException::class)
    override fun flush() {
        sink.flush()
    }

    interface Inker<in T> {
        @Throws(IOException::class)
        fun pen(
            writer: Writer,
            param: T
        ): Boolean
    }

    companion object {

        fun <T> inker(func: (Writer, T) -> Boolean): Inker<T> = object : Inker<T> {
            override
            fun pen(
                writer: Writer,
                param: T
            ) = func(writer, param)
        }

        fun newWriter(out: OutputStream): Writer {
            return Writer(Okio.buffer(Okio.sink(out)))
        }

        fun newWriter(sink: BufferedSink): Writer {
            return Writer(sink)
        }

        private val MODIFIER_INK = object : Inker<Modifier> {
            @Throws(IOException::class)
            override fun pen(
                writer: Writer,
                param: Modifier
            ): Boolean {
                writer.writeString(param.name)
                return true
            }
        }

        private val TYPE_PARAMETER_INK = object : Inker<TypeParameterElement> {
            @Throws(IOException::class)
            override fun pen(
                writer: Writer,
                param: TypeParameterElement
            ): Boolean {
                // Write name
                writer.writeString(param.simpleName.toString())

                // Write bounds
                writer.writeList(ImmutableList.copyOf(param.bounds), TYPE_MIRROR_INK)
                return true
            }
        }

        private val STRING_INK = object : Inker<String> {
            @Throws(IOException::class)
            override fun pen(
                writer: Writer,
                param: String
            ): Boolean {
                writer.writeString(param)
                return true
            }
        }

        private val TYPE_MIRROR_INK = object : Inker<TypeMirror> {
            @Throws(IOException::class)
            override fun pen(
                writer: Writer,
                param: TypeMirror
            ): Boolean {
                return write(writer, TypeName.get(param))
            }
        }

        private val ANNOTATION_INK = object : Inker<AnnotationMirror> {
            @Throws(IOException::class)
            override fun pen(
                writer: Writer,
                param: AnnotationMirror
            ): Boolean {
                val te = param.annotationType.asElement() as TypeElement
                writer.writeClassName(ClassName.get(te))
                return true
            }
        }

        private val PARAM_INK = object : Inker<VariableElement> {
            @Throws(IOException::class)
            override fun pen(
                writer: Writer,
                param: VariableElement
            ): Boolean {
                // Write type
                if (write(writer, TypeName.get(param.asType()))) {
                    // Write name
                    writer.writeString(param.simpleName.toString())

                    // Write modifiers
                    writer.writeModifiers(param.modifiers)

                    // Write annotations
                    writer.writeAnnotations(ImmutableList.copyOf(param.annotationMirrors))
                    return true
                }
                return false
            }
        }
    }
}
