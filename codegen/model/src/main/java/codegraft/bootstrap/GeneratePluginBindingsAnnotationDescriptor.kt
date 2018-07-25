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

import codegraft.AnnotationDescriptor
import codegraft.inject.GeneratePluginBindings
import com.google.auto.common.MoreElements.asType
import com.google.auto.common.MoreTypes.asDeclared
import com.squareup.javapoet.ClassName
import dagger.internal.codegen.getTypeValue
import sourcerer.getAnnotationMirror
import sourcerer.getValue
import javax.inject.Inject
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.TypeElement

data
class GeneratePluginBindingsAnnotationDescriptor(
    override
    val mirror: AnnotationMirror,

    val pluginType: TypeElement,

    val pluginTypeName: String,

    val pluginMapTypeName: String,

    val flattenComponent: Boolean

) : AnnotationDescriptor() {

    fun getMapKeyAnnotationType(
        packageName: String
    ): ClassName = ClassName.get(packageName, "${pluginTypeName}Key")

    class Factory
    @Inject constructor() {
        fun create(
            element: TypeElement
        ): GeneratePluginBindingsAnnotationDescriptor {
            val mirror = element.getAnnotationMirror<GeneratePluginBindings>()!!

            val pluginType = asType(asDeclared(mirror.getTypeValue("pluginType")).asElement())

            val pluginTypeName: String = (mirror.getValue("pluginTypeName") ?: "").let { name ->
                if (name.isNotEmpty()) {
                    name
                } else {
                    "${pluginType.simpleName}"
                }
            }.capitalize()

            val pluginMapTypeName: String = (mirror.getValue("pluginMapTypeName") ?: "").let { name ->
                if (name.isNotEmpty()) {
                    name
                } else {
                    val defaultSuffix = "s"
                    "$pluginTypeName$defaultSuffix"
                }
            }.capitalize()

            val flattenComponent = mirror.getValue("flattenComponent") ?: false

            return GeneratePluginBindingsAnnotationDescriptor(
                mirror = mirror,
                pluginType = pluginType,
                pluginTypeName = pluginTypeName,
                pluginMapTypeName = pluginMapTypeName,
                flattenComponent = flattenComponent
            )
        }
    }
}
