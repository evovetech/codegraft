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
import com.google.auto.common.MoreElements
import com.google.common.collect.ImmutableSet
import sourcerer.AnnotationElements
import sourcerer.AnnotationStep
import sourcerer.AnnotationType
import sourcerer.Output
import sourcerer.Outputs
import sourcerer.processor.ProcessingEnv
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
    var _plugins: Set<GeneratePluginBindingsDescriptor> = LinkedHashSet()

    val plugins: ImmutableSet<GeneratePluginBindingsDescriptor>
        get() = _plugins.toImmutableSet()

    val storedPlugins: ImmutableSet<GeneratePluginBindingsDescriptor> by lazy {
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
        _plugins += descriptors
        return descriptors
                .map(outputFactory::create)
                .flatMap(GeneratePluginBindingsGenerator::process)
    }

    override
    fun postRound(roundEnv: RoundEnvironment): Outputs {
        val allPlugins = plugins + storedPlugins

        getEnv().log("post round")
        val maps = allPlugins.associate { Pair(it, it.element) }
                .mapValues { entry ->
                    val key = entry.key.element
                    val values = roundEnv.getElementsAnnotatedWith(key)
                            .map(MoreElements::asType)
                    getEnv().log("plugin entry: $key=$values")
//                    value.map { MoreElements.asType(it) }
//                            .onEach { getEnv().log("plugin: ${entry.key.element}=$it") }
                    values
                }

//        maps.forEach { k, v ->
//            getEnv().log("plugin maps: ${k.element}=$v")
//        }
//        val typeElements = allPlugins.map { it.element }
//                .onEach { getEnv().log("plugin element: $it") }
//                .flatMap { roundEnv.getElementsAnnotatedWith(it) }
//                .map { MoreElements.asType(it) }

        // TODO:
        return super.postRound(roundEnv)
    }
}
