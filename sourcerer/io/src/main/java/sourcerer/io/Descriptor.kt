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
