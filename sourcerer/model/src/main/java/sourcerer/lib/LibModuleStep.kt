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
import com.squareup.javapoet.TypeSpec
import dagger.Module
import sourcerer.AnnotationStep
import sourcerer.CollectionStep
import sourcerer.Dagger
import sourcerer.Env
import sourcerer.Input
import sourcerer.Intermediate
import sourcerer.JavaOutput
import sourcerer.Klass
import sourcerer.StoredFile
import sourcerer.addAnnotation
import sourcerer.addTo
import sourcerer.inject.LibModule
import sourcerer.intoCollection
import sourcerer.io.Writer
import sourcerer.javaClassOutput
import sourcerer.typesOf
import javax.lang.model.element.Modifier

object LibModuleAnnotationStep : AnnotationStep<LibModule> {
    override
    fun invoke(
        env: Env,
        input: Input<LibModule>
    ) = env.javaClassOutput(input, "LibModule_3", "Generated Lib Module") {
        addModifiers(Modifier.PUBLIC, Modifier.FINAL)
        addAnnotation(Dagger.Module) {
            env.typesOf(input.annotation::includes)
                    .map(ClassName::get)
                    .forEach(addTo("includes"))
        }
        intoCollection<LibModule>()
    }
}

class LibModuleStep(
    isModule: Boolean
) : CollectionStep<LibModule>(
    isModule = isModule,
    inputType = LibModule::class,
    collector = LibModuleAnnotationStep
) {
    override
    fun Writer.writeAll(
        processed: Collection<Intermediate<LibModule>>
    ): Unit = processed.classNames()
            .let { writeTypeNames(it) }

    override
    fun TypeSpec.Builder.writeAll(
        env: Env,
        processed: Collection<Intermediate<LibModule>>,
        stored: Collection<StoredFile>
    ) {
        val classNames = processed.classNames() + stored.readAll()
        addModifiers(Modifier.PUBLIC)
        addAnnotation(Dagger.Module) {
            val addToIncludes = addTo("includes")
            classNames.forEach(addToIncludes)
        }
        intoCollection<Module>()
    }

    private
    fun StoredFile.readAll() = this
            .read { reader -> reader.readTypeNames() }
            .mapNotNull { it as? ClassName }

    private
    fun Collection<StoredFile>.readAll() = this
            .flatMap { it.readAll() }

    private
    fun Collection<Intermediate<LibModule>>.klasses() = this
            .mapNotNull { it.output as? JavaOutput }
            .map(JavaOutput::outKlass)

    private
    fun Collection<Intermediate<LibModule>>.classNames() = this.klasses()
            .map(Klass::rawType)
}
