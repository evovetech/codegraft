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

import sourcerer.AnnotationElements
import sourcerer.AnnotationType
import sourcerer.Output
import sourcerer.ProcessStep
import sourcerer.inject.AndroidInject
import sourcerer.processor.Env
import sourcerer.typeInputs
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidInjectStep
@Inject constructor(
    val descriptorFactory: AndroidInjectModuleDescriptor.Factory,
    val outputFactory: AndroidInjectModuleGenerator.Factory
) : ProcessStep {
    override
    fun Env.annotations(): Set<AnnotationType> = setOf(
        AndroidInject::class
    )

    override
    fun Env.process(annotationElements: AnnotationElements): Map<AnnotationType, List<Output>> {
        val injections = annotationElements.typeInputs<AndroidInject>()
        val descriptors = injections
                .map { it.element }
                .map(descriptorFactory::create)
                .toImmutableSet()
        val outputs = descriptors
                .map(outputFactory::create)
        return outputs.groupBy {
            AndroidInject::class
        }
    }
}
