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

package codegraft.android

import codegraft.android.AndroidInjectModuleDescriptor.Factory
import codegraft.inject.AndroidInject
import com.google.common.collect.ImmutableSet
import sourcerer.AnnotationElements
import sourcerer.AnnotationStep
import sourcerer.AnnotationType
import sourcerer.Output
import sourcerer.Outputs
import sourcerer.processor.ProcessingEnv
import sourcerer.toImmutableSet
import sourcerer.typeInputs
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidInjectStep
@Inject constructor(
    val descriptorFactory: Factory,
    val outputFactory: AndroidInjectModuleGenerator.Factory,
    val sourcerer: AndroidInjectSourcerer
) : AnnotationStep() {
    private
    var _generatedModules: Set<AndroidInjectModuleDescriptor> = LinkedHashSet()

    val generatedModules: ImmutableSet<AndroidInjectModuleDescriptor>
        get() = _generatedModules.toImmutableSet()

    fun storedModules(): ImmutableSet<AndroidInjectModuleDescriptor> = sourcerer
            .storedOutputs()
            .map(descriptorFactory::forStoredModule)
            .toImmutableSet()

    fun sourcererOutput(): Output {
        return sourcerer.output(generatedModules)
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
        _generatedModules += descriptors
        return descriptors
                .map(outputFactory::create)
    }
}
