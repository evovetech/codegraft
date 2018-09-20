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

import sourcerer.io.Reader
import sourcerer.io.Writer
import sourcerer.processor.BaseProcessingEnv
import sourcerer.processor.Env
import sourcerer.processor.ProcessingEnv
import java.net.URL
import java.util.ArrayList
import javax.lang.model.element.Element

/**
 * Created by layne on 2/21/18.
 */

abstract
class BaseEnv(env: Env) : BaseProcessingEnv(env) {
    abstract val file: MetaInf.File
    private val classLoader: ClassLoader = javaClass.classLoader

    abstract fun readAnnotations(annotatedElements: Set<Element>)

    abstract fun Writer.writeSourcererFiles()

    abstract fun Reader.readSourcererFile()

    abstract fun writeJavaFiles(): Any

    fun writeSourcererFiles() {
        val writer = file.newWriter(filer())
        writer.writeSourcererFiles()
        writer.flush()
        writer.close()
    }

    fun readSourcererFiles() = getResources(file.extFilePath())
            .forEach { it.readSourcerFile() }

    private fun URL.readSourcerFile() {
        Reader.newReader(openStream()).use { reader ->
            this@BaseEnv.file.assertCanRead(reader)
            reader.readSourcererFile()
        }
    }
}

fun ProcessingEnv.getResources(path: String): List<URL> {
    val resources = ArrayList<URL>()
    val urls = javaClass.classLoader.getResources(path)
    if (urls.hasMoreElements()) {
        while (urls.hasMoreElements()) {
            val url = urls.nextElement()
            resources.add(url)
            log("url = %s", url)
        }
    } else {
        log("no resource for %s", path)
    }
    return resources
}
