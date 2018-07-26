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