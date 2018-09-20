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

@file:JvmName("TypeNames")

package sourcerer.io

import com.squareup.javapoet.ArrayTypeName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeVariableName
import com.squareup.javapoet.WildcardTypeName
import sourcerer.io.Reader.Companion.parser
import sourcerer.io.Writer.Companion.inker
import java.io.IOException

enum
class Kind(
    private val rw: ReadWriter<*>
) : ReadWriter<TypeName> {
    Primitive(object : ReadWriter<TypeName> {
        override fun canWrite(typeName: TypeName): Boolean {
            return typeName.isPrimitive
        }

        @Throws(IOException::class)
        override fun parse(reader: Reader): TypeName? {
            val className = reader.readClassName()
            return className.unbox()
        }

        @Throws(IOException::class)
        override fun pen(
            writer: Writer,
            param: TypeName
        ): Boolean {
            val className = param.box() as ClassName
            writer.writeClassName(className)
            return true
        }
    }),
    Class(object : AbstractReadWriter<ClassName>(ClassName::class.java) {
        @Throws(IOException::class)
        override fun parse(reader: Reader): ClassName? {
            return reader.readClassName()
        }

        @Throws(IOException::class)
        override fun writeTypeName(
            writer: Writer,
            param: ClassName
        ): Boolean {
            writer.writeClassName(param)
            return true
        }
    }),
    Parameterized(object : AbstractReadWriter<ParameterizedTypeName>(ParameterizedTypeName::class.java) {
        @Throws(IOException::class)
        override fun parse(reader: Reader): ParameterizedTypeName? {
            // Read raw type
            val rawType = reader.readClassName()

            // Write type arguments
            val types = readList(reader)

            // newReader type
            return ParameterizedTypeName.get(rawType, *types.toTypedArray())
        }

        @Throws(IOException::class)
        override fun writeTypeName(
            writer: Writer,
            param: ParameterizedTypeName
        ): Boolean {
            // Write raw type
            writer.writeClassName(param.rawType)

            // Write type arguments
            writeList(writer, param.typeArguments)
            return true
        }
    }),
    TypeVariable(object : AbstractReadWriter<TypeVariableName>(TypeVariableName::class.java) {
        @Throws(IOException::class)
        override fun parse(reader: Reader): TypeVariableName? {
            // Read name
            val name = reader.readString()

            // Read bounds
            val bounds = readList(reader)

            // Create type
            return TypeVariableName.get(name, *bounds.toTypedArray())
        }

        @Throws(IOException::class)
        override fun writeTypeName(
            writer: Writer,
            param: TypeVariableName
        ): Boolean {
            // Write name
            writer.writeString(param.name)

            // Write bounds
            writeList(writer, param.bounds)
            return true
        }
    }),
    Array(object : AbstractReadWriter<ArrayTypeName>(ArrayTypeName::class.java) {
        @Throws(IOException::class)
        override fun parse(reader: Reader): ArrayTypeName? {
            val type = read(reader)
            return if (type == null) null else ArrayTypeName.of(type)
        }

        @Throws(IOException::class)
        override fun writeTypeName(
            writer: Writer,
            param: ArrayTypeName
        ): Boolean {
            return write(writer, param.componentType)
        }
    }),
    Wildcard(object : AbstractReadWriter<WildcardTypeName>(WildcardTypeName::class.java) {
        @Throws(IOException::class)
        override fun parse(reader: Reader): WildcardTypeName? {
            val upperBounds = readList(reader)
            val lowerBounds = readList(reader)

            if (upperBounds.size != 1) {
                println("wrong size for wildcard upper bounds")
                return null
            }

            val upperBound = upperBounds[0]
            if (lowerBounds.isEmpty()) {
                return WildcardTypeName.subtypeOf(upperBound)
            } else if (lowerBounds.size == 1) {
                if (upperBound != TypeName.OBJECT) {
                    print("upper bound should be object but is -> ")
                    log(upperBound)
                    return null
                }
                val lowerBound = lowerBounds[0]
                return WildcardTypeName.supertypeOf(lowerBound)
            }
            println("invalid wildcard")
            return null
        }

        @Throws(IOException::class)
        override fun writeTypeName(
            writer: Writer,
            param: WildcardTypeName
        ): Boolean {
            // Write upper bounds
            writeList(writer, param.upperBounds)

            // Write lower bounds
            writeList(writer, param.lowerBounds)

            return true
        }
    }),
    Unknown(ReadWriter.EMPTY);

    override fun canWrite(typeName: TypeName): Boolean {
        return rw.canWrite(typeName)
    }

    @Throws(IOException::class)
    override fun parse(reader: Reader): TypeName? {
        return rw.parse(reader)
    }

    @Throws(IOException::class)
    override fun pen(
        writer: Writer,
        param: TypeName
    ): Boolean {
        return rw.pen(writer, param)
    }

    companion object {
        fun find(name: String): Kind {
            for (kind in values()) {
                if (kind.name == name) {
                    return kind
                }
            }
            return Unknown
        }

        fun find(typeName: TypeName): Kind {
            for (kind in values()) {
                if (kind.canWrite(typeName)) {
                    return kind
                }
            }
            // Couldn't write type name
            print("couldn't find type name -> ")
            log(typeName)
            return Unknown
        }
    }
}

private interface ReadWriter<out T : TypeName> : Reader.Parser<T>, Writer.Inker<TypeName> {

    fun canWrite(typeName: TypeName): Boolean

    companion object {
        val EMPTY: ReadWriter<TypeName> = object : ReadWriter<TypeName> {
            override fun canWrite(typeName: TypeName): Boolean {
                return false
            }

            @Throws(IOException::class)
            override fun parse(reader: Reader): TypeName? {
                return null
            }

            @Throws(IOException::class)
            override fun pen(
                writer: Writer,
                param: TypeName
            ): Boolean {
                return false
            }
        }
    }
}

private abstract
class AbstractReadWriter<T : TypeName> internal constructor(
    private val type: Class<T>
) : ReadWriter<T> {

    @Throws(IOException::class)
    internal abstract fun writeTypeName(
        writer: Writer,
        param: T
    ): Boolean

    override fun canWrite(typeName: TypeName): Boolean {
        return type.isInstance(typeName)
    }

    @Throws(IOException::class)
    override fun pen(
        writer: Writer,
        param: TypeName
    ): Boolean {
        return canWrite(param) && writeTypeName(writer, type.cast(param))
    }
}

private val TYPE_PARSER = parser { reader ->
    val name = reader.readString()
    val kind = Kind.find(name)
    kind.parse(reader)
}
private val TYPE_INKER = inker<TypeName> { writer, typeName ->
    val kind = Kind.find(typeName)
    writer.writeString(kind.name)
    kind.pen(writer, typeName)
}

@Throws(IOException::class)
fun read(reader: Reader): TypeName? {
    return TYPE_PARSER.parse(reader)
}

@Throws(IOException::class)
fun readList(reader: Reader): List<TypeName> {
    return reader.readList(TYPE_PARSER)
}

@Throws(IOException::class)
fun write(
    writer: Writer,
    typeName: TypeName
): Boolean {
    return TYPE_INKER.pen(writer, typeName)
}

@Throws(IOException::class)
fun writeList(
    writer: Writer,
    typeNames: List<TypeName>
) {
    writer.writeList(typeNames, TYPE_INKER)
}
