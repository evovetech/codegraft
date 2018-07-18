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

import dagger.internal.codegen.BootstrapComponentDescriptor
import dagger.internal.codegen.ComponentBootDataGenerator
import dagger.internal.codegen.ComponentImplementationGenerator
import dagger.internal.codegen.ComponentModuleGenerator
import sourcerer.Includable
import sourcerer.Output
import javax.annotation.processing.FilerException
import javax.inject.Inject

internal
class ComponentOutput(
    val descriptor: BootstrapComponentDescriptor,
    val implementation: ComponentImplementationGenerator,
    val module: ComponentModuleGenerator,
    val bootData: ComponentBootDataGenerator
) {

    class Factory
    @Inject constructor(
        private val componentImplementationFactory: ComponentImplementationGenerator.Factory,
        private val componentModuleFactory: ComponentModuleGenerator.Factory,
        private val componentBootDataFactory: ComponentBootDataGenerator.Factory
    ) {
        fun create(
            descriptors: List<BootstrapComponentDescriptor>
        ): List<ComponentOutput> = try {
            descriptors.map(this::create)
        } catch (_: FilerException) {
            emptyList()
        }

        fun create(
            descriptor: BootstrapComponentDescriptor
        ): ComponentOutput {
            val implementation = componentImplementationFactory.create(descriptor)
            return ComponentOutput(
                descriptor = descriptor,
                implementation = implementation,
                module = componentModuleFactory.create(descriptor, implementation),
                bootData = componentBootDataFactory.create(descriptor)
            )
        }
    }

    val outputs: List<Output> = listOf(
        implementation,
        module,
        bootData
    ).filter(Includable::include)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ComponentOutput

        return descriptor == other.descriptor
    }

    override fun hashCode(): Int {
        return descriptor.hashCode()
    }
}