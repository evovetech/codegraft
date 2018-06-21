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

package dagger.internal.codegen

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import dagger.internal.codegen.SrcComponentDescriptor.Factory
import dagger.internal.codegen.SrcComponentDescriptor.Kind
import okio.Okio
import sourcerer.AnnotationElements
import sourcerer.AnnotationType
import sourcerer.Env
import sourcerer.Output
import sourcerer.ProcessStep
import sourcerer.SourcererOutput
import sourcerer.StoredFile
import sourcerer.getResources
import sourcerer.inject.BootstrapComponent
import sourcerer.io.Reader
import sourcerer.io.Writer
import sourcerer.metaFile
import sourcerer.processor.ProcessingEnv
import sourcerer.typeInputs
import java.net.URL
import javax.inject.Inject

class ComponentStep
@Inject internal
constructor(
    val componentFactory: Factory,
    val componentOutputFactory: ComponentOutput.Factory,
    val appComponentStep: AppComponentStep,
    val sourcerer: BootstrapSourcerer
) : ProcessStep {
    override
    fun Env.annotations(): Set<AnnotationType> = Kind.values()
            .map(Kind::annotationType)
            .toSet()

    override
    fun supportedOptions(): Iterable<Option> = Option.values()
            .toSet()

    override
    fun Env.process(
        annotationElements: AnnotationElements
    ): Map<AnnotationType, List<Output>> {
        val map = HashMap<AnnotationType, List<Output>>()
        val generatedComponents = annotationElements.typeInputs<BootstrapComponent>()
                .map(componentFactory::forComponent)
                .map(componentOutputFactory::create)
        val generatedOutputs = generatedComponents.flatMap(ComponentOutput::outputs)
        val sourcererOutput = sourcerer.output(generatedComponents)

        val storedComponents = sourcerer.storedOutputs()
                .map(componentFactory::forStoredComponent)
                .map(componentOutputFactory::create)
        log("storedComponents = $storedComponents")

        val appComponent = appComponentStep.process(generatedComponents, storedComponents)
        val appComponentOutputs = appComponent.flatMap(AppComponentStep.Output::outputs)

        // outputs
        map[BootstrapComponent::class] = generatedOutputs + sourcererOutput + appComponentOutputs
        return map
    }

    enum
    class Option(
        override val key: String,
        override val defaultValue: String
    ) : sourcerer.processor.Env.Option {
        Package(
            "evovetech.processor.package",
            "evovetech.processor"
        );
    }

    internal
    class BootstrapSourcerer
    @Inject constructor(
        val env: ProcessingEnv,
        val types: SourcererTypes,
        val elements: SourcererElements
    ) {
        internal
        val file = ClassName.get(BootstrapComponent::class.java).metaFile

        internal
        fun output(outputs: List<ComponentOutput>): SrcOutput {
            return componentOutput(outputs.map {
                it.descriptor
            })
        }

        internal
        fun storedOutputs() = env.getResources(file.extFilePath())
                .map(this::load)
                .readAll()

        private
        fun componentOutput(components: List<SrcComponentDescriptor>): SrcOutput {
            return SrcOutput(this, components.map {
                ClassName.get(it.definitionType)
            })
        }

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
    }

    class SrcInput(
        private val storedFiles: Collection<StoredFile>
    ) {
        fun StoredFile.read(): List<TypeName> = read {
            it.readTypeNames()
        }

        fun readAll() = storedFiles.flatMap { it.read() }
                .mapNotNull { it as? ClassName }
    }

    internal
    class SrcOutput(
        private val bootstrap: BootstrapSourcerer,
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
