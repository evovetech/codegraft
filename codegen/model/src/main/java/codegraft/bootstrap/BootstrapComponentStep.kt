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

package codegraft.bootstrap

import codegraft.bootstrap.BootstrapComponentStep.Option
import codegraft.bootstrap.ComponentOutput.Factory
import codegraft.inject.BootstrapComponent
import com.google.common.collect.ImmutableSet
import dagger.internal.codegen.BootstrapComponentDescriptor
import sourcerer.AnnotationElements
import sourcerer.AnnotationStep
import sourcerer.AnnotationType
import sourcerer.DeferredOutput
import sourcerer.Output
import sourcerer.Outputs
import sourcerer.processor.ProcessingEnv
import sourcerer.processor.ProcessingEnv.Options
import sourcerer.toImmutableSet
import sourcerer.typeInputs
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BootstrapComponentStep
@Inject internal
constructor(
    val componentFactory: BootstrapComponentDescriptor.Factory,
    val componentOutputFactory: Factory,
    val sourcerer: BootstrapSourcerer
) : AnnotationStep() {
    internal
    var processed: Boolean = true

    private
    val _generatedComponents = LinkedHashSet<BootstrapComponentDescriptor>()

    internal
    val generatedComponents: ImmutableSet<BootstrapComponentDescriptor>
        get() = _generatedComponents
                .toImmutableSet()

    internal
    fun storedComponents(): ImmutableSet<BootstrapComponentDescriptor> = sourcerer.storedOutputs()
            .map(componentFactory::forStoredComponent)
            .toImmutableSet()

    internal
    fun sourcererOutput(): Output {
        return sourcerer.output(_generatedComponents)
    }

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
