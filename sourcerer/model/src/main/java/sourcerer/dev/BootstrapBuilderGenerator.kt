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

package sourcerer.dev

import com.google.auto.common.MoreTypes
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import dagger.BindsInstance
import org.jetbrains.annotations.Nullable
import sourcerer.BaseElement
import sourcerer.Env
import sourcerer.addAnnotation
import sourcerer.addTo
import sourcerer.inject.BootstrapBuilder
import sourcerer.interfaceBuilder
import sourcerer.toKlass
import sourcerer.typeSpec
import javax.lang.model.element.Modifier.ABSTRACT
import javax.lang.model.element.Modifier.PUBLIC

class BootstrapBuilderGenerator(
    private val descriptor: ComponentDescriptor,
    private val env: Env,
    override val rawType: ClassName = ClassName.get(descriptor.definitionType)
) : BaseElement {
    override
    val outExt: String = "BootstrapBuilder2"

    override
    fun newBuilder() = outKlass.interfaceBuilder()

    override
    fun typeSpec() = typeSpec {
        addModifiers(PUBLIC)
        addAnnotation(ClassName.get(BootstrapBuilder::class.java).toKlass()) {
            descriptor.modules
                    .mapNotNull { ClassName.get(it.definitionType) }
                    .forEach(addTo("modules"))
        }

        // add method for each module
        val applicationModules = descriptor.applicationModules.map { module ->
            val type = module.definitionType
            val name = type.simpleName.toString().decapitalize()
            val param = ParameterSpec.builder(ClassName.get(type), name).run {
                addAnnotation(Nullable::class.java)
                build()
            }
            MethodSpec.methodBuilder(name).run {
                addAnnotation(BindsInstance::class.java)
                addModifiers(PUBLIC, ABSTRACT)
                addParameter(param)
                build()
            }
        }

        env.log("applicationModules = $applicationModules")
        addMethods(applicationModules)

        val dependencies = descriptor.modules.flatMap { it.dependencies }
        val dependencyMethods = dependencies.map { dep ->
            val key = dep.key
            val type = MoreTypes.asDeclared(key.type)
            val element = type.asElement()
            val name = element.simpleName.toString().decapitalize()
            val param = ParameterSpec.builder(ClassName.get(type), name).run {
                key.qualifier?.let {
                    addAnnotation(AnnotationSpec.get(it))
                }
                build()
            }
            MethodSpec.methodBuilder(name).run {
                addAnnotation(BindsInstance::class.java)
                addModifiers(PUBLIC, ABSTRACT)
                addParameter(param)
                build()
            }

        }

        env.log("dependencyMethods = $dependencyMethods")
        addMethods(dependencyMethods)
    }
}
