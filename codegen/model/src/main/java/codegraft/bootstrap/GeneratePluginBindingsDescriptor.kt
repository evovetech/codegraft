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

import codegraft.AnnotatedElementDescriptor
import codegraft.packageName
import com.google.auto.common.MoreElements
import com.squareup.javapoet.ClassName
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
        fun forStoredModule(
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
