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
        metaFile: File,
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
        val metaFile: File,
        val value: T
    )

    inner
    class File : Descriptor.File, FileConstants {
        val FORMAT = "Cannot read from the source. '%s' should be '%s', but is '%s'"

        constructor(file: File) : super(file)

        constructor(fileName: String) : super(fileName)

        @Throws(IOException::class)
        override fun writeTo(writer: Writer) {
            writer.write(FileConstants.HEADER)
            writer.write(FileConstants.VERSION)
        }

        @Throws(IOException::class)
        fun assertCanRead(reader: Reader) {
            val header = Value64.read(reader)
            if (FileConstants.HEADER != header) {
                val msg = String.format(Locale.US, FORMAT, "Header",
                    FileConstants.HEADER, header)
                throw UnknownHeaderException(msg)
            }
            val version = Value64.read(reader)
            if (FileConstants.VERSION != version) {
                val msg = String.format(Locale.US, FORMAT, "Version",
                    FileConstants.VERSION, version)
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
        ): File {
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
            metaFile: File,
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
            return "${DIR}/$dir"
        }
    }
}
