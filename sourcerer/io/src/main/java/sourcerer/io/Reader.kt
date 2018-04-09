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
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeVariableName
import okio.BufferedSource
import okio.ByteString
import okio.Okio
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.util.ArrayList
import java.util.HashSet
import java.util.UUID
import javax.lang.model.element.Modifier

class Reader
private constructor(
    private val source: BufferedSource
) : Closeable {

    @Throws(IOException::class)
    fun read(): ByteString {
        val length = source.readInt()
        log("readInt: '%d'", length)
        val bs = source.readByteString(length.toLong())
        log("readString: '%s'", bs.utf8())
        return bs
    }

    @Throws(IOException::class)
    fun readString(): String {
        val s = read().utf8()
        if (s.isEmpty()) {
            throw IllegalStateException("string is empty")
        }
        return s
    }

    @Throws(IOException::class)
    fun <T> readList(parser: Parser<T>): List<T> {
        val size = source.readInt()
        val uuid = UUID.randomUUID()
        log("readList(%s) size=%d", uuid, size)
        if (size == 0) {
            return emptyList()
        }
        val list = ImmutableList.builder<T>()
        for (i in 0 until size) {
            val t = parser.parse(this)
            log("readList(%s) entry=%s", uuid, t)
            if (t != null) {
                list.add(t)
            } else {
                log("readList i=%d is null", i)
            }
        }
        return list.build()
    }

    @Throws(IOException::class)
    fun readStringList(): List<String> {
        return readList(STRING_PARSER)
    }

    @Throws(IOException::class)
    fun readModifiers(): Set<Modifier> {
        return HashSet(readList(MODIFIER_PARSER))
    }

    @Throws(IOException::class)
    fun readTypeParams(): List<TypeVariableName> {
        return readList(TYPE_PARAMETER_PARSER)
    }

    @Throws(IOException::class)
    fun readClassName(): ClassName {
        val packageName = readString()
        val simpleNames = ArrayList(readStringList())
        val simpleName = simpleNames.removeAt(0)
        return if (simpleNames.size == 0) {
            ClassName.get(packageName, simpleName)
        } else ClassName.get(packageName, simpleName, *simpleNames.toTypedArray())
    }

    @Throws(IOException::class)
    fun readTypeName(): TypeName? {
        return read(this)
    }

    @Throws(IOException::class)
    fun readTypeNames(): List<TypeName> {
        return readList(this)
    }

    @Throws(IOException::class)
    fun readParams(): List<ParameterSpec> {
        return readList(PARAM_PARSER)
    }

    @Throws(IOException::class)
    fun readAnnotations(): List<AnnotationSpec> {
        return readList(ANNOTATION_PARSER)
    }

    @Throws(IOException::class)
    override fun close() {
        source.close()
    }

    interface Parser<out T> {
        @Throws(IOException::class)
        fun parse(reader: Reader): T?
    }

    companion object {

        fun <T : Any> parser(func: (Reader) -> T?): Parser<T> = object : Parser<T> {
            override
            fun parse(reader: Reader): T? = func(reader)
        }

        fun newReader(`is`: InputStream): Reader {
            return Reader(Okio.buffer(Okio.source(`is`)))
        }

        fun newReader(source: BufferedSource): Reader {
            return Reader(source)
        }

        private val MODIFIER_PARSER = object : Parser<Modifier> {
            @Throws(IOException::class)
            override fun parse(reader: Reader): Modifier? {
                val name = reader.readString()
                for (modifier in Modifier.values()) {
                    if (name.equals(modifier.name, ignoreCase = true)) {
                        return modifier
                    }
                }
                return null
            }
        }

        private val TYPE_PARAMETER_PARSER = object : Parser<TypeVariableName> {
            @Throws(IOException::class)
            override fun parse(reader: Reader): TypeVariableName {
                // Read name
                val name = reader.readString()

                // Read bounds
                val bounds = reader.readTypeNames()

                // Create type
                return TypeVariableName.get(name, *bounds.toTypedArray())
            }
        }

        private val STRING_PARSER = object : Parser<String> {
            @Throws(IOException::class)
            override fun parse(reader: Reader): String? {
                return reader.readString()
            }
        }

        private val CLASSNAME_PARSER = object : Parser<ClassName> {
            @Throws(IOException::class)
            override fun parse(reader: Reader): ClassName {
                return reader.readClassName()
            }
        }

        private val ANNOTATION_PARSER = object : Parser<AnnotationSpec> {
            @Throws(IOException::class)
            override fun parse(reader: Reader): AnnotationSpec {
                return AnnotationSpec.builder(reader.readClassName())
                        // TODO: code blocks
                        .build()
            }
        }

        private val PARAM_PARSER = object : Parser<ParameterSpec> {
            @Throws(IOException::class)
            override fun parse(reader: Reader): ParameterSpec? {
                // Read type
                val type = reader.readTypeName()
                if (type != null) {
                    // Read name
                    val name = reader.readString()

                    // Read modifiers
                    val paramModifiers = reader.readModifiers()

                    // Create param spec with modifiers
                    return ParameterSpec.builder(type, name)
                            .addModifiers(*paramModifiers.toTypedArray())
                            // Read annotations
                            .addAnnotations(reader.readList(ANNOTATION_PARSER))
                            // build
                            .build()
                }
                return null
            }
        }
    }
}
