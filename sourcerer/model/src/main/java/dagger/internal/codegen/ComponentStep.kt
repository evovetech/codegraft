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
import dagger.internal.codegen.ComponentStep.Option
import sourcerer.AnnotationElements
import sourcerer.AnnotationType
import sourcerer.DeferredOutput
import sourcerer.Output
import sourcerer.ProcessStep
import sourcerer.inject.BootstrapComponent
import sourcerer.processor.Env
import sourcerer.processor.ProcessingEnv
import sourcerer.processor.ProcessingEnv.Options
import sourcerer.typeInputs
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ComponentStep
@Inject internal
constructor(
    val componentFactory: Factory,
    val componentOutputFactory: ComponentOutput.Factory,
    val appComponentStep: AppComponentStep,
    val sourcerer: BootstrapSourcerer
) : ProcessStep {
    internal
    var processed: Boolean = true

    internal
    val generatedComponents = ArrayList<ComponentOutput>()

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
            generatedComponents += bootstrapComponents
                    .map(componentFactory::forComponent)
                    .map(componentOutputFactory::create)
            val generatedOutputs = generatedComponents.flatMap(ComponentOutput::outputs)
            val sourcererOutput = sourcerer.output(generatedComponents)

            // outputs
            map[BootstrapComponent::class] = generatedOutputs + sourcererOutput

            processed = true
        } catch (e: TypeNotPresentException) {
            map[BootstrapComponent::class] = bootstrapComponents.map {
                DeferredOutput(it.element)
            }
            processed = false
        }
        return map
    }

    fun Env.postProcess(): List<Output> {
        if (!processed) {
            return emptyList()
        }

        val storedComponents = sourcerer.storedOutputs()
                .map(componentFactory::forStoredComponent)
                .map(componentOutputFactory::create)
        log("storedComponents = $storedComponents")
        val appComponent = appComponentStep.process(generatedComponents, storedComponents)
        return appComponent.flatMap(AppComponentStep.Output::outputs)
    }

    enum
    class Option(
        override val key: String,
        override val defaultValue: String
    ) : ProcessingEnv.Option {
        Package(
            "evovetech.processor.package",
            "evovetech.processor"
        ),
        Application(
            "evovetech.processor.application",
            "android.app.Application"
        );
    }
}

val Options.Package: String
    get() = this[Option.Package]
val Options.Application: Class<*>?
    get() = try {
        Class.forName(this[Option.Application])
    } catch (_: Throwable) {
        null
    }
