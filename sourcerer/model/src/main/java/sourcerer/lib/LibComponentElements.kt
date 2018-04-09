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
import sourcerer.BaseElements
import sourcerer.Codegen
import sourcerer.entryList
import sourcerer.getOrCreate
import sourcerer.io.Writer
import java.util.HashMap
import javax.annotation.processing.Filer

/**
 * Created by layne on 2/21/18.
 */

class LibComponentElements(
    val modules: LibModuleElements
) : HashMap<ClassName, LibComponentElement>(),
    BaseElements<LibComponentElement> {

    override fun create(key: ClassName) =
        LibComponentElement(key)

    fun addAll(
        element: LibComponentElement
    ) = getOrCreate(element.rawType).let {
        it.modules.addAll(element.modules)
        && it.dependencies.addAll(element.dependencies)
    }

    override fun writeTo(writer: Writer) {
        writer.writeList(entryList()) { _, entry ->
            val element = entry.value
            writer.writeClassName(entry.key)
            writer.writeTypeNames(element.modules.toList())
            writer.writeTypeNames(element.dependencies.toList())
            true
        }
    }

    override fun writeJavaFiles(filer: Filer) = values.map {
        it.writeTo(filer)
        modules.addAll(it.libModule())
        it.outKlass.rawType
    }.run {
        LibComponentElement(Codegen.LibComponents.rawType, dependencies = this)
    }.apply {
        writeTo(filer)
        modules.addAll(libModule())
    }
}
