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

package sourcerer.dev

import sourcerer.AnnotatedTypeElement
import sourcerer.AnnotationElements
import sourcerer.AnnotationType
import sourcerer.Env
import sourcerer.Output
import sourcerer.ProcessStep
import sourcerer.inject.BootstrapComponent
import sourcerer.typeInputs
import javax.inject.Inject
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

data
class ComponentDescriptor(
    val componentDefinitionType: TypeElement
) {
    class Factory
    @Inject constructor(
        val elements: Elements,
        val types: Types
    ) {
        fun forComponent(
            componentTypeElement: AnnotatedTypeElement<*>
        ): ComponentDescriptor {
            return ComponentDescriptor(componentTypeElement.element)
        }
    }
}

class BootstrapComponentStep
@Inject constructor(
    val componentFactory: ComponentDescriptor.Factory
) : ProcessStep {
    override
    fun Env.annotations(): Set<AnnotationType> = setOf(
        BootstrapComponent::class
    )

    override
    fun supportedOptions(): Iterable<Option> = Option.values()
            .toSet()

    override
    fun Env.process(
        annotationElements: AnnotationElements
    ): Map<AnnotationType, List<Output>> {
        val opt = options
        log("")
        log("options=$opt")
        log("package=${opt[Option.Package]}")
        log("")
        val components = annotationElements.typeInputs<BootstrapComponent>()
                .map(componentFactory::forComponent)
        log("bootstrapComponents=$components")
        return emptyMap()
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


