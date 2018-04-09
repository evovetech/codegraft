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
import com.squareup.javapoet.TypeName
import sourcerer.BaseElements
import sourcerer.Codegen
import sourcerer.entryList
import sourcerer.getOrCreate
import sourcerer.io.Writer
import java.util.ArrayList
import java.util.HashMap
import javax.annotation.processing.Filer

/**
 * Created by layne on 2/21/18.
 */

class LibModuleElements :
    HashMap<ClassName, LibModuleElement>(),
    BaseElements<LibModuleElement> {

    override fun create(key: ClassName) =
        LibModuleElement(key)

    fun addAll(
        value: LibModuleElement
    ) = addAll(value.rawType, value)

    fun addAll(
        key: ClassName,
        value: Collection<ClassName>
    ) = getOrCreate(key)
            .addAll(value)

    override fun writeTo(writer: Writer) {
        writer.writeList(entryList()) { _, entry ->
            val list = ArrayList<TypeName>(entry.value)
            writer.writeClassName(entry.key)
            writer.writeTypeNames(list)
            true
        }
    }

    override fun writeJavaFiles(filer: Filer) = values.map {
        it.writeTo(filer)
        it.outKlass.rawType
    }.run {
        LibModuleElement(Codegen.LibModules.rawType, this)
    }.apply {
        writeTo(filer)
    }
}
