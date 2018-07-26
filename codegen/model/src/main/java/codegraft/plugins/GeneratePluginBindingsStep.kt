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
import com.google.common.collect.HashMultimap
import sourcerer.AnnotationElements
import sourcerer.AnnotationStep
import sourcerer.AnnotationType
import sourcerer.Output
import sourcerer.Outputs
import sourcerer.processor.ProcessingEnv
import sourcerer.toImmutableMap
import sourcerer.toImmutableSet
import sourcerer.typeInputs
import javax.annotation.processing.RoundEnvironment
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeneratePluginBindingsStep
@Inject constructor(
    val descriptorFactory: GeneratePluginBindingsDescriptor.Factory,
    val outputFactory: GeneratePluginBindingsGenerator.Factory,
    val sourcerer: GeneratePluginBindingsSourcerer
) : AnnotationStep() {

    private
    val _generatedPlugins: MutablePlugins = LinkedHashSet()

    private
    val _generatedModules: MutableModules = HashMultimap.create()

    val generatedPlugins: Plugins
        get() = Plugins.copyOf(_generatedPlugins)

    val generatedModules: Modules
        get() = Modules.copyOf(_generatedModules)

    val storedModules: Modules by lazy {
        val builder: ModulesBuilder = Modules.builder()
        sourcerer.storedOutputs()
                .map(descriptorFactory::forStoredModules)
                .onEach { (parent, children) ->
                    builder.putAll(parent, children)
                }
        builder.build()
    }

    val storedPlugins: Plugins by lazy {
        storedModules.keySet().toImmutableSet()
    }

    fun sourcererOutput(): Output {
        return sourcerer.output(generatedModules)
    }

    override
    fun ProcessingEnv.annotations(): Set<AnnotationType> = setOf(
        GeneratePluginBindings::class
    )

    override
    fun ProcessingEnv.process(annotationElements: AnnotationElements): Outputs {
        val injections = annotationElements.typeInputs<GeneratePluginBindings>()
                .map { it.element }
        val descriptors = injections
                .map(descriptorFactory::create)
                .toImmutableSet()
        _generatedPlugins.addAll(descriptors)
        return descriptors
                .map(outputFactory::create)
                .flatMap(GeneratePluginBindingsGenerator::process)
    }

    override
    fun postRound(roundEnv: RoundEnvironment): Outputs {
        val allPlugins = (generatedPlugins + storedPlugins)
                .toImmutableSet()
        val allModules = allPlugins.associate { descriptor ->
            val key = descriptor
            val value = descriptorFactory.modules(key, roundEnv)
            Pair(key, value)
        }.toImmutableMap()

        allModules.map { (key, value) ->
            _generatedModules.putAll(key, value)
        }
        return allModules.flatMap {
            it.value.flatMap { it.outputs() }
        }
    }
}

