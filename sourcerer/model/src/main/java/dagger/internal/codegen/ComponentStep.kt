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

import dagger.internal.codegen.BootstrapComponentDescriptor.Factory
import dagger.internal.codegen.BootstrapComponentDescriptor.Kind
import sourcerer.AnnotationElements
import sourcerer.AnnotationType
import sourcerer.DeferredOutput
import sourcerer.Env
import sourcerer.Output
import sourcerer.ProcessStep
import sourcerer.inject.BootstrapComponent
import sourcerer.typeInputs
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
        val bootstrapComponents = annotationElements.typeInputs<BootstrapComponent>()
        try {
            val generatedComponents = bootstrapComponents
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
        } catch (e: TypeNotPresentException) {
            map[BootstrapComponent::class] = bootstrapComponents.map {
                DeferredOutput(it.element)
            }
        }
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
}
