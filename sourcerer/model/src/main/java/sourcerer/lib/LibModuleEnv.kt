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

package sourcerer.lib

import com.squareup.javapoet.ClassName
import sourcerer.getOrCreate
import sourcerer.inject.LibModule
import sourcerer.io.Reader
import sourcerer.io.Writer
import sourcerer.processor.Env
import sourcerer.typesOf
import java.io.IOException
import java.util.AbstractMap
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

class LibModuleEnv(
    env: Env
) : BaseEnv2<LibModuleElements>(
    env = env,
    name = "LibModules",
    elements = LibModuleElements()
) {
    override fun readAnnotations(annotatedElements: Set<Element>) {
        // Iterate over all @LibModule annotated elements
        for (annotatedElement in annotatedElements) {
            val te = annotatedElement as TypeElement
            val className = ClassName.get(te)
            val classes = elements.getOrCreate(className)
            val module = annotatedElement.getAnnotation(LibModule::class.java)
            typesOf(module::includes)
                    .mapTo(classes) { ClassName.get(it) }
        }
    }

    override fun Writer.writeSourcererFiles() {
        write(elements)
    }

    override fun Reader.readSourcererFile() {
        for (entry in readList<Map.Entry<ClassName, Set<ClassName>>>(Parser)) {
            elements.addAll(entry.key, entry.value)
        }
    }

    override fun writeJavaFiles() =
        elements.writeJavaFiles(filer())

    object Parser : Reader.Parser<Map.Entry<ClassName, Set<ClassName>>> {
        @Throws(IOException::class)
        override fun parse(reader: Reader): Map.Entry<ClassName, Set<ClassName>> {
            val className = reader.readClassName()
            val list = reader.readTypeNames()
                    .map { it as ClassName }
                    .toSet()
            return AbstractMap.SimpleEntry<ClassName, Set<ClassName>>(className, list)
        }
    }
}
