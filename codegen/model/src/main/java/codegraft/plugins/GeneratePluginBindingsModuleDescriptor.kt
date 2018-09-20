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

import codegraft.ElementDescriptor
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import sourcerer.JavaOutput
import sourcerer.Outputs
import sourcerer.addTo
import sourcerer.getFieldName
import sourcerer.interfaceBuilder
import sourcerer.typeSpec
import javax.lang.model.element.Modifier.ABSTRACT
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.STATIC
import javax.lang.model.element.TypeElement

class GeneratePluginBindingsModuleDescriptor(

    val parent: GeneratePluginBindingsDescriptor,

    override
    val element: TypeElement

) : ElementDescriptor<TypeElement>() {

    fun output(): Out = Out(this)

    fun outputs(): Outputs {
        return listOf(output())
    }

    class Out(
        val descriptor: GeneratePluginBindingsModuleDescriptor
    ) : JavaOutput(
        rawType = ClassName.get(descriptor.element),
        outExt = "Module"
    ) {
        val parent = descriptor.parent

        override
        fun newBuilder() =
            outKlass.interfaceBuilder()

        override
        fun typeSpec() = typeSpec {
            addModifiers(PUBLIC, STATIC)
            addAnnotation(Module::class.java)

            val returnType = parent.annotation.pluginType.className
            val paramType = descriptor.element.className
            val param = ParameterSpec.builder(paramType, paramType.getFieldName()).run {
                // TODO:
                build()
            }

            val pluginTypeName = parent.annotation.pluginTypeName
            addMethod(MethodSpec.methodBuilder("bind$pluginTypeName").run {
                addModifiers(PUBLIC, ABSTRACT)

                addAnnotation(Binds::class.java)
                addAnnotation(IntoMap::class.java)
                addAnnotation(AnnotationSpec.builder(parent.mapKeyAnnotationType).run {
                    paramType.let(addTo("value"))
                    build()
                })

                addParameter(param)
                returns(returnType)
                build()
            })
        }
    }
}
