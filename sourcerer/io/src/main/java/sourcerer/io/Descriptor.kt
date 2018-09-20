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

package sourcerer.io

import com.google.common.base.MoreObjects
import com.google.common.base.Objects
import okio.BufferedSink
import okio.Okio
import java.io.IOException
import java.util.jar.JarEntry
import javax.annotation.processing.Filer
import javax.tools.StandardLocation

abstract
class Descriptor(
    private val dir: String,
    private val ext: String
) {
    fun dir(): String {
        return dir
    }

    fun fileExtension(): String {
        return ext
    }

    abstract
    fun file(fileName: String): File

    override
    fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Descriptor) return false
        val that = other as Descriptor?
        return Objects.equal(dir, that!!.dir) && Objects.equal(ext, that.ext)
    }

    override
    fun hashCode(): Int {
        return Objects.hashCode(dir, ext)
    }

    override
    fun toString(): String {
        return MoreObjects.toStringHelper(this)
                .add("dir", dir)
                .add("fileExtension", ext)
                .toString()
    }

    abstract inner
    class File(
        private val fileName: String
    ) : Writeable {

        protected constructor(file: File) : this(file.fileName) {}

        fun fileName(): String {
            return fileName
        }

        fun extFileName(): String {
            return fileName + ext
        }

        fun extFilePath(): String {
            return dir + "/" + extFileName()
        }

        fun matches(jarEntry: JarEntry): Boolean {
            return extFilePath() == jarEntry.name
        }

        @Throws(IOException::class)
        fun newWriter(filer: Filer): Writer {
            val output = filer.createResource(StandardLocation.CLASS_OUTPUT, "", extFilePath())
            val sink = Okio.buffer(Okio.sink(output.openOutputStream()))
            return newWriter(sink)
        }

        /* visible for testing */
        @Throws(IOException::class)
        fun newWriter(sink: BufferedSink): Writer {
            val writer = Writer.newWriter(sink)
            writeTo(writer)
            return writer
        }

        open
        fun descriptor(): Descriptor {
            return this@Descriptor
        }

        override
        fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is File) return false
            val that = other as File?
            return Objects.equal(descriptor(), that!!.descriptor()) && Objects.equal(fileName, that.fileName)
        }

        override
        fun hashCode(): Int {
            return Objects.hashCode(fileName)
        }

        override
        fun toString(): String {
            return MoreObjects.toStringHelper(this)
                    .add("fileName", fileName)
                    .toString()
        }
    }
}
