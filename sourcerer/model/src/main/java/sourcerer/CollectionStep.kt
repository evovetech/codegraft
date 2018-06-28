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

import com.squareup.javapoet.TypeSpec
import okio.Okio
import sourcerer.inject.Generated
import sourcerer.inject.IntoCollection
import sourcerer.io.Reader
import sourcerer.io.Writer
import sourcerer.processor.Env
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass

abstract
class IntoCollectionStep(
    subType: KClass<out Annotation>
) : FullStep,
    SingleStep<IntoCollection> {

    override
    val inputType = IntoCollection::class

    val qualifiedName: String = subType.java.canonicalName

    abstract
    fun process(
        env: Env,
        inputs: List<Input<IntoCollection>>
    ): Output

    final override
    fun invoke(
        env: Env,
        input: AnnotationElements
    ): Collection<Output> {
        val inputs = input.inputs<IntoCollection>()
                .filter { qualifiedName == env.typeOf(it.annotation::value).qualifiedName.toString() }
                .onEach { env.log(it.element, "@IntoCollection<$qualifiedName>") }
        val output = process(env, inputs)
        return listOf(output)
    }
}

//cla

abstract
class CollectionStep<A : Annotation>(
    val isModule: Boolean,
    final override val inputType: KClass<A>,
    val collector: AnnotationStep<A>,
    val processGeneratedFiles: Boolean = true
) : FullStep,
    SingleStep<A> {
    private val rawType = inputType.className
    private val file = rawType.metaFile
    private val processed = ArrayList<Intermediate<A>>()
    private val written = AtomicBoolean()

    open
    fun comments(): String = ""

    override
    fun annotationTypes(): Set<KClass<out Annotation>> {
        val superTypes = super.annotationTypes()
        if (processGeneratedFiles) {
            return superTypes + Generated::class
        }
        return superTypes
    }

    abstract
    fun Writer.writeAll(processed: Collection<Intermediate<A>>)

    abstract
    fun TypeSpec.Builder.writeAll(
        env: Env,
        processed: Collection<Intermediate<A>>,
        stored: Collection<StoredFile>
    )

    override
    fun invoke(
        env: Env,
        input: AnnotationElements
    ): Collection<Output> {
        val intermediates = input.inputs(inputType.java).map {
            val output = collector(env, it)
            Intermediate(it, output)
        }
        if (!intermediates.isEmpty()) {
            processed.addAll(intermediates)
            return processed.map(Intermediate<A>::output)
        }

        val output = if (!written.compareAndSet(false, true)) {
            NoOutput
        } else if (isModule) {
            srcrOutput()
        } else {
            javaOutput(env)
        }
        return listOf(output)
    }

    private
    fun javaOutput(
        env: Env
    ) = env.javaClassOutput(rawType, "Collection", comments()) {
        writeAll(env, processed, env.storedOutputs())
    }

    private
    fun srcrOutput() = object : SourcererOutput() {
        override
        fun file() = file

        override
        fun write(writer: Writer) =
            writer.writeAll(processed)
    }

    private
    fun load(url: URL) = Okio.buffer(Okio.source(url.openStream())).use { source ->
        file.assertCanRead(Reader.newReader(source))
        StoredFile(source.readByteArray())
    }

    private
    fun Env.storedOutputs() = getResources(file.extFilePath())
            .map { load(it) }
}
