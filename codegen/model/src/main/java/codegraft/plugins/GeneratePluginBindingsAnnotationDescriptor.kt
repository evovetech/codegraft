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
