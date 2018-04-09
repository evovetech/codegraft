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

import com.google.common.base.Joiner
import com.google.common.collect.ImmutableList
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import sourcerer.io.Reader
import sourcerer.io.Writeable
import sourcerer.io.Writer
import java.io.IOException
import java.util.ArrayList
import java.util.LinkedHashSet
import javax.lang.model.element.TypeElement

class Extension
private constructor(
    private val kind: ExtensionClass.Kind,
    private val packageName: String,
    private val className: String,
    private val typeName: ClassName = typeName(packageName, className)
) {
    private
    val qualifiedName: String

    private
    constructor(
        kind: String,
        typeName: ClassName
    ) : this(
        ExtensionClass.Kind.fromName(kind),
        typeName
    )

    private
    constructor(
        kind: ExtensionClass.Kind,
        typeName: ClassName
    ) : this(
        kind,
        typeName.packageName(),
        className(typeName),
        typeName
    )

    init {
        this.qualifiedName = qualifiedName(typeName)
    }

    fun newProcessor(): Processor {
        return Processor()
    }

    fun kind(): ExtensionClass.Kind {
        return kind
    }

    fun packageName(): String {
        return packageName
    }

    fun className(): String {
        return className
    }

    fun qualifiedName(): String {
        return qualifiedName
    }

    fun typeName(): ClassName {
        return typeName
    }

    fun javaPackagePath(): String {
        return packageName().replace('.', '/')
    }

    fun javaFileName(): String {
        return className() + ".java"
    }

    override
    fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Extension) return false

        val key = other as Extension?

        return if (kind != key!!.kind) false else qualifiedName == key.qualifiedName
    }

    override
    fun hashCode(): Int {
        var result = kind.hashCode()
        result = 31 * result + qualifiedName.hashCode()
        return result
    }

    class Sourcerer
    private constructor(
        private val extension: Extension,
        methods: Collection<MethodSpec>
    ) {
        private
        val methods: ImmutableList<MethodSpec> = ImmutableList.copyOf(methods)

        fun extension(): Extension {
            return extension
        }

        fun methods(): ImmutableList<MethodSpec> {
            return methods
        }

        fun newSourceWriter(): SourceWriter {
            return SourceWriter(this)
        }

        companion object {
            private val PARSER = object : Reader.Parser<Sourcerer> {
                @Throws(IOException::class)
                override fun parse(reader: Reader): Sourcerer {
                    val kind = reader.readString()
                    val typeName = reader.readClassName()
                    val extension = Extension(kind, typeName)

                    val parser = ExtensionClassHelper.parser(typeName)
                    val methods = ImmutableList.builder<MethodSpec>()
                    for (m in reader.readList(parser)) {
                        methods.addAll(m)
                    }
                    return Sourcerer(extension, methods.build())
                }
            }

            fun create(
                extension: Extension,
                methods: Collection<MethodSpec>
            ): Sourcerer {
                return Sourcerer(extension, methods)
            }

            @Throws(IOException::class)
            fun read(reader: Reader): Sourcerer? {
                return PARSER.parse(reader)
            }

            @Throws(IOException::class)
            fun readList(reader: Reader): List<Sourcerer> {
                return reader.readList(PARSER)
            }
        }
    }

    inner
    class Processor
    internal constructor() : Writeable {
        private
        val classHelpers: LinkedHashSet<ExtensionClassHelper> = LinkedHashSet()

        fun extension(): Extension {
            return this@Extension
        }

        fun process(typeElement: TypeElement): Boolean {
            val classHelper = ExtensionClassHelper.process(kind(), typeElement)
            System.out.printf("\nparsed class: %s, extClass: %s\n", typeElement, classHelper)
            val result: Boolean = synchronized(classHelpers) {
                classHelpers.add(classHelper)
            }
            System.out.printf("\nthis = %s\n", this)
            return result
        }

        @Throws(IOException::class)
        override fun writeTo(writer: Writer) {
            // Write extension
            writer.writeString(kind.name)
            writer.writeClassName(typeName)

            val list: List<ExtensionClassHelper> = synchronized(classHelpers) {
                ArrayList(classHelpers)
            }
            writer.writeList(list)
        }
    }

    companion object {
        internal fun create(ext: ExtensionClass): Extension {
            return Extension(ext.kind, ext.packageName, ext.className)
        }

        private fun className(typeName: ClassName): String {
            return className(typeName.simpleNames())
        }

        private fun className(simpleNames: List<String>): String {
            return Joiner.on('.')
                    .join(simpleNames)
        }

        private fun typeName(
            packageName: String,
            className: String
        ): ClassName {
            if (className.isEmpty()) throw IllegalArgumentException("empty className")
            val index = className.indexOf('.')
            if (index == -1) {
                return ClassName.get(packageName, className)
            }

            // Add the class names, like "Map" and "Entry".
            val parts = className.substring(index + 1).split("\\.".toRegex()).toTypedArray()
            return ClassName.get(packageName, className, *parts)
        }

        private fun qualifiedName(typeName: ClassName): String {
            val packageName = typeName.packageName()
            if (packageName.isEmpty()) {
                return className(typeName)
            }
            val names = ArrayList(typeName.simpleNames())
            names.add(0, packageName)
            return Joiner.on('.')
                    .join(names)
        }
    }
}
