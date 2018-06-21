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

import com.squareup.javapoet.ClassName
import sourcerer.AnnotationElements
import sourcerer.AnnotationType
import sourcerer.Env
import sourcerer.Output
import sourcerer.ProcessStep
import sourcerer.SourcererOutput
import sourcerer.inject.ApplicationComponent
import sourcerer.inject.BootstrapComponent
import sourcerer.io.Writer
import sourcerer.metaFile
import sourcerer.typeInputs
import javax.inject.Inject

class ComponentStep
@Inject constructor(
    val componentFactory: ComponentDescriptor.Factory,
    val bootstrapComponentStep: BootstrapComponentStep,
    val appComponentStep: AppComponentStep
) : ProcessStep {
    override
    fun Env.annotations(): Set<AnnotationType> = ComponentDescriptor.Kind.values()
            .map(ComponentDescriptor.Kind::annotationType)
            .toSet()

    override
    fun supportedOptions(): Iterable<Option> = Option.values()
            .toSet()

    /*
    override
    fun Env.process(
        annotationElements: AnnotationElements
    ): Map<AnnotationType, List<Output>> {
        val map = HashMap<AnnotationType, List<Output>>()
        map[BootstrapComponent::class] = annotationElements.typeInputs<BootstrapComponent>()
                .map(componentFactory::forComponent)
                .let(bootstrapComponentStep::process)
        map[ApplicationComponent::class] = annotationElements.typeInputs<ApplicationComponent>()
                .map(componentFactory::forComponent)
                .let(appComponentStep::process)
        return map
    }
     */

    override
    fun Env.process(
        annotationElements: AnnotationElements
    ): Map<AnnotationType, List<Output>> {
        val map = HashMap<AnnotationType, List<Output>>()
        val bootstrapComponents = annotationElements.typeInputs<BootstrapComponent>()
                .map(componentFactory::forComponent)
                .let(bootstrapComponentStep::process)
        val bootstrapOutputs = bootstrapComponents.flatMap(BootstrapComponentStep.Output::outputs)
        val srcrOutput = bootstrapComponents.map { it.component.descriptor }
                .let { SrcrOutput(it) }
        map[BootstrapComponent::class] = bootstrapOutputs + srcrOutput

        val appComponent = appComponentStep.process(bootstrapComponents)
        map[ApplicationComponent::class] = appComponent.flatMap(AppComponentStep.Output::outputs)

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

    class SrcrOutput(
        bootstrapComponents: List<ComponentDescriptor>
    ) : SourcererOutput() {
        private val file = ClassName.get(BootstrapComponent::class.java).metaFile
        private val classes = bootstrapComponents.map {
            ClassName.get(it.definitionType)
        }

        override
        fun file() = file

        override
        fun write(writer: Writer) {
            writer.writeTypeNames(classes)
        }
    }
}
