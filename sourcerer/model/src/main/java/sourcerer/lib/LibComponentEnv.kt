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
import sourcerer.BaseEnv
import sourcerer.MetaInf
import sourcerer.getOrCreate
import sourcerer.inject.LibComponent
import sourcerer.io.Reader
import sourcerer.io.Writer
import sourcerer.typesOf
import java.io.IOException
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

/**
 * Created by layne on 2/21/18.
 */

class LibComponentEnv(env: LibModuleEnv) : BaseEnv(env) {
    val elements: LibComponentElements = LibComponentElements(env.elements)
    override val file: MetaInf.File = MetaInf.create("sourcerer")
            .file("LibComponents")

    @Throws(IOException::class)
    override fun readAnnotations(annotatedElements: Set<Element>) {
        for (annotatedElement in annotatedElements) {
            val te = annotatedElement as TypeElement
            val className = ClassName.get(te)
            val classes = elements.getOrCreate(className).apply {
                dependencies.add(className)
            }
            val component = annotatedElement.getAnnotation(LibComponent::class.java)
            typesOf(component::modules)
                    .mapTo(classes.modules, ClassName::get)
            typesOf(component::dependencies)
                    .mapTo(classes.dependencies, ClassName::get)
        }
    }

    override fun Writer.writeSourcererFiles() {
        write(elements)
    }

    override fun Reader.readSourcererFile() {
        readList(Parser).forEach {
            elements.addAll(it)
        }
    }

    override fun writeJavaFiles() =
        elements.writeJavaFiles(filer())

    object Parser : Reader.Parser<LibComponentElement> {
        override fun parse(reader: Reader): LibComponentElement {
            val className = reader.readClassName()
            val modules = reader.readTypeNames()
                    .map { it as ClassName }
                    .toMutableSet()
            val dependencies = reader.readTypeNames()
                    .map { it as ClassName }
                    .toMutableSet()
            return LibComponentElement(className, modules, dependencies)
        }
    }
}
