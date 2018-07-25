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
import com.google.common.collect.HashMultimap
import com.google.common.collect.ImmutableMultimap
import com.google.common.collect.ImmutableSet
import com.google.common.collect.Multimap
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

typealias MutablePlugins = MutableSet<GeneratePluginBindingsDescriptor>
typealias Plugins = ImmutableSet<GeneratePluginBindingsDescriptor>

typealias MutableModules = Multimap<GeneratePluginBindingsDescriptor, GeneratePluginBindingsModuleDescriptor>
typealias Modules = ImmutableMultimap<GeneratePluginBindingsDescriptor, GeneratePluginBindingsModuleDescriptor>
typealias ModulesBuilder = ImmutableMultimap.Builder<GeneratePluginBindingsDescriptor, GeneratePluginBindingsModuleDescriptor>

@Singleton
class GeneratePluginBindingsStep
@Inject constructor(
    val descriptorFactory: GeneratePluginBindingsDescriptor.Factory,
    val outputFactory: GeneratePluginBindingsGenerator.Factory,
    val sourcerer: GeneratePluginBindingsSourcerer
) : AnnotationStep() {

    private
    val _plugins: MutablePlugins = LinkedHashSet()

    private
    val _modules: MutableModules = HashMultimap.create()

    val plugins: Plugins
        get() = Plugins.copyOf(_plugins)

    val modules: Modules
        get() = Modules.copyOf(_modules)

    val storedPlugins: Plugins by lazy {
        sourcerer.storedOutputs()
                .map(descriptorFactory::forStoredModule)
                .toImmutableSet()
    }

    fun sourcererOutput(): Output {
        return sourcerer.output(plugins)
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
        _plugins.addAll(descriptors)
        return descriptors
                .map(outputFactory::create)
                .flatMap(GeneratePluginBindingsGenerator::process)
    }

    override
    fun postRound(roundEnv: RoundEnvironment): Outputs {
        val allPlugins = (plugins + storedPlugins)
                .toImmutableSet()
        val allModules = allPlugins.associate { descriptor ->
            val key = descriptor
            val value = descriptorFactory.modules(key, roundEnv)
            Pair(key, value)
        }.toImmutableMap()

        allModules.map { (key, value) ->
            _modules.putAll(key, value)
        }

//        return allPlugins.associate { descriptor ->
//            val key = descriptor.element
//            val values = roundEnv.getElementsAnnotatedWith(key)
//                    .map(MoreElements::asType)
//            Pair(descriptor, values)
//        }.flatMap { (descriptor, elements) ->
//            elements.map { element ->
//                GeneratePluginBindingsModuleDescriptor(descriptor, element)
//            }
//        }
        return emptyList()
    }
}

