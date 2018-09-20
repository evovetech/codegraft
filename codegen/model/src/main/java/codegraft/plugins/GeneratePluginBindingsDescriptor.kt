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

package codegraft.plugins

import codegraft.AnnotatedElementDescriptor
import codegraft.packageName
import com.google.auto.common.MoreElements
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import sourcerer.qualifiedName
import javax.annotation.processing.RoundEnvironment
import javax.inject.Inject
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

data
class GeneratePluginBindingsDescriptor(
    override
    val element: TypeElement,

    override
    val annotation: GeneratePluginBindingsAnnotationDescriptor

) : AnnotatedElementDescriptor<TypeElement>() {

    val mapKeyAnnotationType: ClassName
        get() = annotation.getMapKeyAnnotationType(packageName)

    class Factory
    @Inject constructor(
        val elements: Elements,
        val types: Types,
        val annotationFactory: GeneratePluginBindingsAnnotationDescriptor.Factory
    ) {
        fun stored(
            className: ClassName
        ): GeneratePluginBindingsDescriptor {
            val typeElement = elements.getTypeElement(className.qualifiedName)
            return create(typeElement)
        }

        fun create(
            element: TypeElement
        ): GeneratePluginBindingsDescriptor {
            val annotation = annotationFactory.create(element)
            return GeneratePluginBindingsDescriptor(
                element = element,
                annotation = annotation
            )
        }

        fun forStoredModules(
            pair: Pair<ClassName, List<TypeName>>
        ): Pair<GeneratePluginBindingsDescriptor, List<GeneratePluginBindingsModuleDescriptor>> {
            val parent = stored(pair.first)
            return forStoredModules(parent, pair.second)
        }

        private
        fun forStoredModules(
            parent: GeneratePluginBindingsDescriptor,
            children: List<TypeName>
        ): Pair<GeneratePluginBindingsDescriptor, List<GeneratePluginBindingsModuleDescriptor>> {
            val modules = children.filterIsInstance<ClassName>()
                    .map { className -> elements.getTypeElement(className.qualifiedName) }
                    .map { element -> GeneratePluginBindingsModuleDescriptor(parent, element) }
            return Pair(parent, modules)
        }

        fun modules(
            descriptor: GeneratePluginBindingsDescriptor,
            roundEnv: RoundEnvironment
        ): List<GeneratePluginBindingsModuleDescriptor> {
            val annotationType = descriptor.element
            return roundEnv.getElementsAnnotatedWith(annotationType)
                    .map { element ->
                        GeneratePluginBindingsModuleDescriptor(
                            descriptor,
                            MoreElements.asType(element)
                        )
                    }
        }
    }
}
