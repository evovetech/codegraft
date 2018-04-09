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

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableListMultimap
import com.google.common.collect.ListMultimap
import sourcerer.exceptions.IncompatibleVersionException
import sourcerer.exceptions.UnknownHeaderException
import sourcerer.io.Descriptor
import sourcerer.io.Reader
import sourcerer.io.Value64
import sourcerer.io.Writer
import java.io.IOException
import java.util.Locale
import java.util.jar.JarEntry
import java.util.jar.JarInputStream

class MetaInf
private constructor(
    dir: String
) : Descriptor(dir(dir), FILE_EXTENSION) {

    fun parseFile(entry: JarEntry): File? {
        val path = entry.name
        val dir = dir()
        if (path.startsWith(dir)) {
            val other = path.substring(dir.length, path.length)
            if (other.length >= 2 && other.startsWith("/")) {
                return file(other.substring(1, other.length))
            }
        }
        return null
    }

    override fun file(fileName: String): File {
        return File(fileName)
    }

    @Throws(IOException::class)
    private fun <T> entry(
        jar: JarInputStream,
        jarEntry: JarEntry,
        parser: Reader.Parser<T>
    ): Entry<T>? {
        val metaFile = parseFile(jarEntry)
        return if (metaFile != null) {
            entry(jar, metaFile, parser)
        } else null
    }

    @Throws(IOException::class)
    private fun <T> entry(
        jar: JarInputStream,
        metaFile: MetaInf.File,
        parser: Reader.Parser<T>
    ): Entry<T> {
        val reader = Reader.newReader(jar)
        metaFile.assertCanRead(reader)
        val value = parser.parse(reader)!!
        return Entry(metaFile, value)
    }

    interface FileConstants {
        companion object {
            val HEADER = Value64.from("sourcerer")
            val VERSION = Value64.from(1)
        }
    }

    data
    class Entry<out T>(
        val metaFile: MetaInf.File,
        val value: T
    )

    inner
    class File : Descriptor.File, FileConstants {
        val FORMAT = "Cannot read from the source. '%s' should be '%s', but is '%s'"

        constructor(file: File) : super(file)

        constructor(fileName: String) : super(fileName)

        @Throws(IOException::class)
        override fun writeTo(writer: Writer) {
            writer.write(MetaInf.FileConstants.HEADER)
            writer.write(MetaInf.FileConstants.VERSION)
        }

        @Throws(IOException::class)
        fun assertCanRead(reader: Reader) {
            val header = Value64.read(reader)
            if (MetaInf.FileConstants.HEADER != header) {
                val msg = String.format(Locale.US, FORMAT, "Header", MetaInf.FileConstants.HEADER, header)
                throw UnknownHeaderException(msg)
            }
            val version = Value64.read(reader)
            if (MetaInf.FileConstants.VERSION != version) {
                val msg = String.format(Locale.US, FORMAT, "Version", MetaInf.FileConstants.VERSION, version)
                throw IncompatibleVersionException(msg)
            }
        }

        override fun descriptor(): MetaInf {
            return this@MetaInf
        }
    }

    companion object {
        private val DIR = "META-INF/sourcerer"
        private val FILE_EXTENSION = ".srcr"

        fun create(dir: String): MetaInf {
            return MetaInf(dir)
        }

        fun file(
            dir: String,
            fileName: String
        ): MetaInf.File {
            return MetaInf(dir)
                    .file(fileName)
        }

        @Throws(IOException::class)
        fun <T> fromJar(
            metaList: List<MetaInf>,
            jar: JarInputStream,
            parser: Reader.Parser<T>
        ): ListMultimap<MetaInf, Entry<T>> {
            val map = ImmutableListMultimap.builder<MetaInf, Entry<T>>()
            var jarEntry: JarEntry? = jar.nextJarEntry
            while (jarEntry != null) {
                for (metaInf in metaList) {
                    val entry = metaInf.entry(jar, jarEntry, parser)
                    if (entry != null) {
                        map.put(metaInf, entry)
                    }
                }
                jarEntry = jar.nextJarEntry
            }
            return map.build()
        }

        @Throws(IOException::class)
        fun <T> fromJar(
            metaFile: MetaInf.File,
            jar: JarInputStream,
            parser: Reader.Parser<T>
        ): List<Entry<T>> {
            val metaInf = metaFile.descriptor()
            val list = ImmutableList.builder<Entry<T>>()
            var jarEntry: JarEntry? = jar.nextJarEntry
            while (jarEntry != null) {
                if (metaFile.matches(jarEntry)) {
                    list.add(metaInf.entry(jar, metaFile, parser))
                }
                jarEntry = jar.nextJarEntry
            }
            return list.build()
        }

        @Throws(IOException::class)
        fun <T> fromJar(
            metaInf: MetaInf,
            jar: JarInputStream,
            parser: Reader.Parser<T>
        ): List<Entry<T>> {
            val list = ImmutableList.builder<Entry<T>>()
            var jarEntry: JarEntry? = jar.nextJarEntry
            while (jarEntry != null) {
                val entry = metaInf.entry(jar, jarEntry, parser)
                if (entry != null) {
                    list.add(entry)
                }
                jarEntry = jar.nextJarEntry
            }
            return list.build()
        }

        private fun dir(dir: String): String {
            if (dir.isEmpty()) throw IllegalArgumentException("dir must not be empty")
            return "$DIR/$dir"
        }
    }
}
