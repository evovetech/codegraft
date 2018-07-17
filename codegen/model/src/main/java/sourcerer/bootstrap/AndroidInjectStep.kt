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

package sourcerer.bootstrap

import com.google.common.collect.ImmutableSet
import sourcerer.AnnotationElements
import sourcerer.AnnotationStep
import sourcerer.AnnotationType
import sourcerer.Output
import sourcerer.Outputs
import sourcerer.inject.AndroidInject
import sourcerer.processor.ProcessingEnv
import sourcerer.toImmutableSet
import sourcerer.typeInputs
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidInjectStep
@Inject constructor(
    val descriptorFactory: AndroidInjectModuleDescriptor.Factory,
    val outputFactory: AndroidInjectModuleGenerator.Factory,
    val sourcerer: AndroidInjectSourcerer
) : AnnotationStep() {
    private
    var _modules: Set<AndroidInjectModuleDescriptor> = LinkedHashSet()

    val modules: ImmutableSet<AndroidInjectModuleDescriptor>
        get() = _modules.toImmutableSet()

    fun storedModules(): ImmutableSet<AndroidInjectModuleDescriptor> = sourcerer
            .storedOutputs()
            .map(descriptorFactory::forStoredModule)
            .toImmutableSet()

    fun sourcererOutput(): Output {
        return sourcerer.output(modules)
    }

    override
    fun ProcessingEnv.annotations(): Set<AnnotationType> = setOf(
        AndroidInject::class
    )

    override
    fun ProcessingEnv.process(annotationElements: AnnotationElements): Outputs {
        val injections = annotationElements.typeInputs<AndroidInject>()
                .map { it.element }
        val descriptors = injections
                .map(descriptorFactory::create)
                .toImmutableSet()
        _modules += descriptors
        return descriptors
                .map(outputFactory::create)
    }
}
