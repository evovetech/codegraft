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

package sourcerer

import com.google.common.base.MoreObjects
import com.google.common.collect.ImmutableList
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import sourcerer.ExtensionClass.Kind.StaticDelegate
import sourcerer.ExtensionMethod.Kind.ReturnThis
import sourcerer.ExtensionMethod.Kind.Void
import sourcerer.io.Reader
import sourcerer.io.Writeable
import sourcerer.io.Writer
import java.io.IOException
import java.util.ArrayList
import java.util.HashSet
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

internal
class ExtensionClassHelper
private constructor(
    private val kind: ExtensionClass.Kind,
    private val element: TypeElement,
    private val instanceMethod: ExtensionMethodHelper?,
    methods: List<ExtensionMethodHelper>
) : Writeable {
    private val methods: ImmutableList<ExtensionMethodHelper>
    private val methodInk: MethodInk

    init {
        if (instanceMethod == null) {
            throw IllegalArgumentException(element.qualifiedName.toString() + " must have an instance method specified")
        } else if (ClassName.get(element) != TypeName.get(instanceMethod.method.returnType)) {
            throw IllegalArgumentException(
                element.qualifiedName.toString() + " instance method must return its own type"
            )
        } else if (methods.isEmpty()) {
            throw IllegalArgumentException(element.qualifiedName.toString() + " has no annotated methods to process")
        }
        this.methods = ImmutableList.copyOf(methods)
        this.methodInk = MethodInk()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val that = other as ExtensionClassHelper?

        return element.qualifiedName.toString() == that!!.element.qualifiedName.toString()
    }

    override fun hashCode(): Int {
        return element.qualifiedName.toString().hashCode()
    }

    @Throws(IOException::class)
    override fun writeTo(writer: Writer) {
        writer.writeList(methods, methodInk)
    }

    override fun toString(): String {
        return MoreObjects.toStringHelper(this)
                .add("element", element)
                .add("kind", kind)
                .toString()
    }

    private
    class MethodParser(
        private val type: TypeName
    ) : Reader.Parser<MethodSpec> {

        @Throws(IOException::class)
        override fun parse(reader: Reader): MethodSpec {
            // Read method name
            val methodName = reader.readString()

            // Read modifiers
            val modifiers = reader.readModifiers()

            // Create method builder with modifiers
            val methodBuilder = MethodSpec.methodBuilder(methodName)
                    .addModifiers(*modifiers.toTypedArray())
                    // Read type parameters
                    .addTypeVariables(reader.readTypeParams())
                    // Read Parameters
                    .addParameters(reader.readParams())
                    // Read return annotations
                    .addAnnotations(reader.readAnnotations())

            // Read classType
            val classType = reader.readClassName()

            // Read statement
            var statement = reader.readString()

            // Read method kind
            val methodKind = ExtensionMethod.Kind.fromName(reader.readString())
            when (methodKind) {
                ExtensionMethod.Kind.Return -> {
                    val returnType = reader.readTypeName()
                    methodBuilder.returns(returnType)
                    statement = "return $statement"
                    methodBuilder.addStatement(statement, classType)
                }
                ReturnThis -> {
                    methodBuilder.returns(type)
                    methodBuilder.addStatement(statement, classType)
                    methodBuilder.addStatement("return this")
                }
                Void -> methodBuilder.addStatement(statement, classType)
                else -> throw IllegalStateException("invalid method kind")
            }

            // build
            return methodBuilder.build()
        }
    }

    private
    inner class MethodInk :
        Writer.Inker<ExtensionMethodHelper> {

        @Throws(IOException::class)
        override fun pen(
            writer: Writer,
            param: ExtensionMethodHelper
        ): Boolean {
            val methodElement = param.method
            var methodKind = param.kind

            // Write method name
            val methodName = methodElement.simpleName.toString()
            writer.writeString(methodName)

            // Write modifiers
            var modifiers: MutableSet<Modifier> = methodElement.modifiers
            if (kind == StaticDelegate) {
                // add Static Modifier
                modifiers = HashSet(modifiers)
                modifiers.add(Modifier.STATIC)
                if (methodKind == ReturnThis) {
                    methodKind = Void
                }
            }
            writer.writeModifiers(modifiers)

            // Write type parameters
            writer.writeTypeParams(methodElement.typeParameters)

            // Write parameters
            val params = writer.writeParams(methodElement.parameters)

            // Write return annotations
            writer.writeAnnotations(ArrayList<AnnotationMirror>(param.returnAnnotations))

            // Write classType
            writer.writeClassName(ClassName.get(element))

            // Write statement
            val statement = String.format(
                "\$T.%s().%s(%s)",
                instanceMethod?.name(), param.name(), params
            )
            writer.writeString(statement)

            // Write method kind
            writer.writeString(methodKind.name)
            when (methodKind) {
                ExtensionMethod.Kind.Return -> {
                    val returnType = TypeName.get(methodElement.returnType)
                    writer.writeTypeName(returnType)
                }
                ReturnThis, Void -> {
                }
                else -> throw IllegalStateException("invalid method kind")
            }
            return true
        }
    }

    companion object {

        fun process(
            kind: ExtensionClass.Kind,
            element: TypeElement
        ): ExtensionClassHelper {
            var instanceMethod: ExtensionMethodHelper? = null
            val methods = ArrayList<ExtensionMethodHelper>()
            for (memberElement in element.enclosedElements) {
                val method = ExtensionMethodHelper.process(memberElement)
                             ?: continue
                when (method.kind) {
                    ExtensionMethod.Kind.Instance -> {
                        if (instanceMethod != null) {
                            val format = "Cannot have instance method '%s' when '%s' is already defined"
                            val message = String.format(format, method.name(), instanceMethod.name())
                            throw IllegalStateException(message)
                        }
                        instanceMethod = method
                    }
                    else -> methods.add(method)
                }
            }
            return ExtensionClassHelper(kind, element, instanceMethod, methods)
        }

        fun parser(typeName: TypeName): Reader.Parser<List<MethodSpec>> {
            val methodParser = MethodParser(typeName)
            return object : Reader.Parser<List<MethodSpec>> {
                @Throws(IOException::class)
                override fun parse(reader: Reader): List<MethodSpec>? {
                    return reader.readList(methodParser)
                }
            }
        }
    }
}
