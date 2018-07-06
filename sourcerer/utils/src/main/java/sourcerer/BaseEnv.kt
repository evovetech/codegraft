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
