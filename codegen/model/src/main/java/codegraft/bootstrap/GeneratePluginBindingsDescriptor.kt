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

import codegraft.inject.GeneratePluginBindings
import com.google.auto.common.MoreElements.asType
import com.google.auto.common.MoreTypes.asDeclared
import com.google.auto.common.MoreTypes.equivalence
import com.google.common.base.Equivalence
import com.squareup.javapoet.ClassName
import dagger.internal.codegen.getTypeValue
import sourcerer.getAnnotationMirror
import sourcerer.getValue
import sourcerer.qualifiedName
import javax.inject.Inject
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

data
class GeneratePluginBindingsDescriptor(
    val element: TypeElement,

    val annotationMirror: AnnotationMirror,

    val pluginType: TypeElement,

    val pluginTypeName: String,

    val pluginMapTypeName: String,

    val flattenComponent: Boolean
) {
    val type: TypeMirror = element.asType()

    private
    val typeWrapper: Equivalence.Wrapper<TypeMirror> =
        equivalence().wrap(type)

    override
    fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GeneratePluginBindingsDescriptor

        if (typeWrapper != other.typeWrapper) return false

        return true
    }

    override
    fun hashCode(): Int {
        return typeWrapper.hashCode()
    }

    class Factory
    @Inject constructor(
        val elements: Elements,
        val types: Types
    ) {
        fun forStoredModule(
            className: ClassName
        ): GeneratePluginBindingsDescriptor {
            val typeElement = elements.getTypeElement(className.qualifiedName)
            return create(typeElement)
        }

        fun create(element: TypeElement): GeneratePluginBindingsDescriptor {
            val annotationMirror = element.getAnnotationMirror<GeneratePluginBindings>()!!
            val pluginType = asType(asDeclared(annotationMirror.getTypeValue("pluginType")).asElement())
            val pluginTypeName: String = (annotationMirror.getValue("pluginTypeName") ?: "").let { name ->
                if (name.isNotEmpty()) {
                    name
                } else {
                    "${pluginType.simpleName}"
                }
            }.capitalize()
            val pluginMapTypeName: String = (annotationMirror.getValue("pluginMapTypeName") ?: "").let { name ->
                if (name.isNotEmpty()) {
                    name
                } else {
                    val defaultSuffix = "s"
                    "$pluginTypeName$defaultSuffix"
                }
            }.capitalize()
            val flattenComponent = annotationMirror.getValue<Boolean>("flattenComponent") ?: false
            return GeneratePluginBindingsDescriptor(
                element = element,
                annotationMirror = annotationMirror,
                pluginType = pluginType,
                pluginTypeName = pluginTypeName,
                pluginMapTypeName = pluginMapTypeName,
                flattenComponent = flattenComponent
            )
        }
    }
}
