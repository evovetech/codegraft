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
