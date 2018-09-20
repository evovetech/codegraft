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
import com.google.common.collect.HashMultimap
import sourcerer.AnnotationElements
import sourcerer.AnnotationStep
import sourcerer.AnnotationType
import sourcerer.Output
import sourcerer.Outputs
import sourcerer.processor.ProcessingEnv
import sourcerer.toImmutableList
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

    val storedEntries by lazy {
        sourcerer.storedOutputs()
                .map(descriptorFactory::forStoredModules)
                .toImmutableList()
    }

    val storedPlugins: Plugins by lazy {
        storedEntries.map { it.first }
                .toImmutableSet()
    }

    val storedModules: Modules by lazy {
        val builder: ModulesBuilder = Modules.builder()
        storedEntries.onEach { (parent, children) ->
            builder.putAll(parent, children)
        }
        builder.build()
    }

    fun sourcererOutput(): Output {
        return sourcerer.output(
            generatedPlugins,
            generatedModules
        )
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
        getEnv().log("GenPlugin: begin postRound -->")
        allPlugins.map { it.element }
                .let { getEnv().log("allPlugins=$it") }
        val generatedModules = allPlugins.associate { descriptor ->
            val key = descriptor
            val value = descriptorFactory.modules(key, roundEnv)
            value.map { it.element }
                    .let { getEnv().log("key=$key, value=$it") }
            Pair(key, value)
        }.toImmutableMap()
//        getEnv().log("generatedModules=$generatedModules")

        generatedModules.map { (key, value) ->
            _generatedModules.putAll(key, value)
        }
        return generatedModules.flatMap {
            it.value.flatMap { it.outputs() }
        }.apply {
            getEnv().log("<-- GenPlugin: end postRound")
        }
    }
}

