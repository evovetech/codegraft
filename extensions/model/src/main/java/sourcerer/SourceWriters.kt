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
import com.squareup.javapoet.MethodSpec
import okio.Buffer
import sourcerer.io.Reader
import java.io.BufferedInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.ArrayList
import java.util.HashMap
import java.util.jar.JarInputStream
import javax.annotation.processing.Filer

class SourceWriters {
    private val extensions = HashMap<Extension, MutableList<MethodSpec>>()

    @Throws(IOException::class)
    fun read(`is`: InputStream) {
        JarInputStream(BufferedInputStream(`is`)).use { jar -> addAll(Extensions.Sourcerer.fromJar(jar)) }
    }

    /* visible for testing */
    @Throws(IOException::class)
    fun read(source: Buffer) {
        val reader = Reader.newReader(source)
        val metaFile = Extensions.instance().file()
        metaFile.assertCanRead(reader)
        addAll(Extensions.Sourcerer.read(reader))
    }

    private fun addAll(map: Map<Extension, MutableList<MethodSpec>>) {
        synchronized(extensions) {
            for ((ext, value) in map) {
                var methods: MutableList<MethodSpec>? = extensions[ext]
                if (methods == null) {
                    methods = ArrayList()
                    extensions[ext] = methods
                }
                methods.addAll(value)
            }
        }
    }

    @Throws(IOException::class)
    fun writeTo(filer: Filer) {
        for (sourceWriter in sourceWriters()) {
            sourceWriter.writeTo(filer)
        }
    }

    @Throws(IOException::class)
    fun writeTo(outputDir: File) {
        for (sourceWriter in sourceWriters()) {
            sourceWriter.writeTo(outputDir)
        }
    }

    private fun sourceWriters(): List<SourceWriter> {
        val sourceWriters = ImmutableList.builder<SourceWriter>()
        synchronized(extensions) {
            for ((key, value) in extensions) {
                val sourcerer = Extension.Sourcerer.create(key, value)
                sourceWriters.add(sourcerer.newSourceWriter())
            }
        }
        return sourceWriters.build()
    }
}
