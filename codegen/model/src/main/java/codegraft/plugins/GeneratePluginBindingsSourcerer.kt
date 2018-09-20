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
    fun output(
        plugins: Plugins,
        modules: Modules
    ): Output {
        return Output(this, plugins, modules)
    }

    internal
    fun storedOutputs() = env.getResources(file.extFilePath())
            .mapNotNull(this::load)
            .readAll()

    private
    fun load(url: URL) = try {
        Okio.buffer(Okio.source(url.openStream())).use { source ->
            file.assertCanRead(Reader.newReader(source))
            StoredFile(source.readByteArray())
        }
    } catch (e: Throwable) {
        e.printStackTrace()
        null
    }

    private
    fun StoredFile.read(): List<Pair<ClassName, List<TypeName>>> = read {
        it.readList(ModuleReadWrite)
    }

    private
    fun Collection<StoredFile>.readAll() = flatMap { it.read() }

    class Output(
        private val sourcerer: GeneratePluginBindingsSourcerer,
        private val plugins: Plugins,
        private val modules: Modules
    ) : SourcererOutput() {

        override
        fun file() = sourcerer.file

        override
        fun write(writer: Writer) {
            val groups = plugins.groups(modules)
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
fun Plugins.groups(modules: Modules): List<ModuleGroup> {
    val allKeys = this + modules.keySet()
    return allKeys.map { parent ->
        val children = modules.get(parent)
                .toImmutableSet()
        ModuleGroup(parent, children)
    }
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
