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

package codegraft.bootstrap

import codegraft.inject.GeneratePluginBindings
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

class GeneratePluginBindingsSourcerer
@Inject internal
constructor(
    val env: ProcessingEnv
) {
    internal
    val file = ClassName.get(GeneratePluginBindings::class.java).metaFile

    internal
    fun output(modules: Collection<GeneratePluginBindingsDescriptor>): Output {
        return Output(this, modules.map {
            ClassName.get(it.annotationType)
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
        private val bootstrap: GeneratePluginBindingsSourcerer,
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
