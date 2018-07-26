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

package codegraft.plugins

import codegraft.inject.GeneratePluginBindings
import com.google.common.collect.ImmutableSet
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import okio.Okio
import sourcerer.SourcererOutput
import sourcerer.StoredFile
import sourcerer.getResources
import sourcerer.io.Reader
import sourcerer.io.Writer
import sourcerer.metaFile
import sourcerer.processor.ProcessingEnv
import sourcerer.toImmutableSet
import java.net.URL
import javax.inject.Inject
import javax.lang.model.element.TypeElement

class GeneratePluginBindingsSourcerer
@Inject internal
constructor(
    val env: ProcessingEnv
) {
    internal
    val file = ClassName.get(GeneratePluginBindings::class.java).metaFile

    internal
    fun output(modules: Modules): Output {
        return Output(this, modules)
    }

    internal
    fun storedOutputs() = env.getResources(file.extFilePath())
            .map(this::load)
            .readAll()

    private
    fun load(url: URL) = Okio.buffer(Okio.source(url.openStream())).use { source ->
        file.assertCanRead(Reader.newReader(source))
        StoredFile(source.readByteArray())
    }

    private
    fun StoredFile.read(): List<Pair<ClassName, List<TypeName>>> = read {
        it.readList(ModuleReadWrite)
    }

    private
    fun Collection<StoredFile>.readAll() = flatMap { it.read() }

    class Output(
        private val sourcerer: GeneratePluginBindingsSourcerer,
        private val modules: Modules
    ) : SourcererOutput() {

        override
        fun file() = sourcerer.file

        override
        fun write(writer: Writer) {
            val groups = modules.groups()
            writer.writeList(groups, ModuleReadWrite)
        }
    }
}

internal
class ModuleGroup(
    val parent: GeneratePluginBindingsDescriptor,
    val children: ImmutableSet<GeneratePluginBindingsModuleDescriptor>
)

internal
fun Modules.groups(): List<ModuleGroup> = keySet().map { parent ->
    val children = get(parent).toImmutableSet()
    ModuleGroup(parent, children)
}

private
object ModuleReadWrite :
    Reader.Parser<Pair<ClassName, List<TypeName>>>,
    Writer.Inker<ModuleGroup> {

    override
    fun parse(reader: Reader): Pair<ClassName, List<TypeName>> {
        // read parent type
        val parent = reader.readClassName()

        // read children types
        val children = reader.readTypeNames()

        return Pair(parent, children)
    }

    override
    fun pen(writer: Writer, param: ModuleGroup): Boolean {
        // write parent type
        param.parent.element.className
                .let(writer::writeClassName)

        // write children types
        param.children
                .map(GeneratePluginBindingsModuleDescriptor::element)
                .map(TypeElement::className)
                .let(writer::writeTypeNames)
        return true
    }
}
