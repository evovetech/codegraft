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

package codegraft.android

import codegraft.inject.AndroidInject
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
import java.net.URL
import javax.inject.Inject

class AndroidInjectSourcerer
@Inject internal
constructor(
    val env: ProcessingEnv
) {
    internal
    val file = ClassName.get(AndroidInject::class.java).metaFile

    internal
    fun output(modules: Collection<AndroidInjectModuleDescriptor>): Output {
        return Output(this, modules.map {
            ClassName.get(it.element)
        })
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
    fun StoredFile.read(): List<TypeName> = read {
        it.readTypeNames()
    }

    private
    fun Collection<StoredFile>.readAll() = flatMap { it.read() }
            .mapNotNull { it as? ClassName }

    class Output(
        private val bootstrap: AndroidInjectSourcerer,
        private val typeNames: List<TypeName>
    ) : SourcererOutput() {
        override
        fun file() = bootstrap.file

        override
        fun write(writer: Writer) {
            writer.writeTypeNames(typeNames)
        }
    }
}
