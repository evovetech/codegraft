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
import dagger.internal.codegen.BootstrapComponentDescriptor
import sourcerer.AnnotationElements
import sourcerer.AnnotationType
import sourcerer.DeferredOutput
import sourcerer.bootstrap.ComponentStep.Option
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
    val componentFactory: BootstrapComponentDescriptor.Factory,
    val componentOutputFactory: ComponentOutput.Factory
) : AnnotationStep() {
    internal
    var processed: Boolean = true

    private
    val _generatedComponents = LinkedHashSet<BootstrapComponentDescriptor>()

    internal
    val generatedComponents: ImmutableSet<BootstrapComponentDescriptor>
        get() = _generatedComponents
                .toImmutableSet()

    override
    fun Env.annotations(): Set<AnnotationType> = setOf(
        BootstrapComponent::class
    )

    override
    fun supportedOptions(): Iterable<Option> = Option.values()
            .toSet()

    override
    fun ProcessingEnv.process(
        annotationElements: AnnotationElements
    ): Outputs {
        val bootstrapComponents = annotationElements.typeInputs<BootstrapComponent>()
        return try {
            val genComponents = bootstrapComponents
                    .map(componentFactory::forComponent)
                    .toImmutableSet()
            val generatedOutputs = genComponents
                    .map(componentOutputFactory::create)
                    .flatMap(ComponentOutput::outputs)
            _generatedComponents += genComponents

            processed = true

            // outputs
            generatedOutputs
        } catch (e: TypeNotPresentException) {
            processed = false
            bootstrapComponents.map {
                DeferredOutput(it.element)
            }
        }
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
