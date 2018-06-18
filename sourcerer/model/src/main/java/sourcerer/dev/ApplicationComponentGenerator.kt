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
import com.squareup.javapoet.TypeName
import dagger.BindsInstance
import sourcerer.BaseElement
import sourcerer.Env
import sourcerer.Klass
import sourcerer.SourceWriter
import sourcerer.addAnnotation
import sourcerer.addTo
import sourcerer.inject.ApplicationComponent
import sourcerer.interfaceBuilder
import sourcerer.toKlass
import sourcerer.typeSpec
import javax.lang.model.element.Modifier.ABSTRACT
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.STATIC

class ApplicationComponentGenerator(
    private val descriptor: ComponentDescriptor,
    private val env: Env,
    override val rawType: ClassName = ClassName.get(descriptor.definitionType)
) : BaseElement {
    override
    val outExt: String = "ApplicationComponent2"

    override
    fun newBuilder() = outKlass.interfaceBuilder()

    override
    fun typeSpec() = typeSpec {
        addModifiers(PUBLIC)
        addSuperinterface(TypeName.get(descriptor.definitionType.asType()))
        addAnnotation(ClassName.get(ApplicationComponent::class.java).toKlass()) {
            descriptor.applicationModules
                    .map(ModuleDescriptor::definitionType)
                    .mapNotNull(ClassName::get)
                    .forEach(addTo("modules"))
        }
        addType(Builder().typeSpec())

//        // add method for each module
//        val applicationModules = descriptor.applicationModules.map { module ->
//            val type = module.definitionType
//            val name = type.simpleName.toString().decapitalize()
//            val param = ParameterSpec.builder(ClassName.get(type), name).run {
//                addAnnotation(Nullable::class.java)
//                build()
//            }
//            MethodSpec.methodBuilder(name).run {
//                addAnnotation(BindsInstance::class.java)
//                addModifiers(PUBLIC, ABSTRACT)
//                addParameter(param)
//                build()
//            }
//        }
//
//        env.log("applicationModules = $applicationModules")
//        addMethods(applicationModules)
//
//        val dependencies = descriptor.modules.flatMap { it.dependencies }
//        val dependencyMethods = dependencies.map { dep ->
//            val key = dep.key
//            val type = MoreTypes.asDeclared(key.type)
//            val element = type.asElement()
//            val name = element.simpleName.toString().decapitalize()
//            val param = ParameterSpec.builder(ClassName.get(type), name).run {
//                key.qualifier?.let {
//                    addAnnotation(AnnotationSpec.get(it))
//                }
//                build()
//            }
//            MethodSpec.methodBuilder(name).run {
//                addAnnotation(BindsInstance::class.java)
//                addModifiers(PUBLIC, ABSTRACT)
//                addParameter(param)
//                build()
//            }
//
//        }
//
//        env.log("dependencyMethods = $dependencyMethods")
//        addMethods(dependencyMethods)
    }

    inner
    class Builder : SourceWriter {
        override
        val outKlass: Klass = "Builder".toKlass()

        override
        fun newBuilder() = outKlass.interfaceBuilder()

        override
        fun typeSpec() = typeSpec {
            addModifiers(PUBLIC, STATIC)
            addAnnotation(ApplicationComponent.Builder::class.java)

            // TODO: override all methods for clarity
//            ElementFilter.methodsIn()

            // add method for each module
            descriptor.applicationModules.map { module ->
                val type = module.definitionType
                val name = type.simpleName.toString().decapitalize()
                MethodSpec.methodBuilder(name).run {
                    addModifiers(PUBLIC, ABSTRACT)
                    addParameter(ClassName.get(type), name)
                    build()
                }
            }.map(this::addMethod)

            val bindings = descriptor.modules
                    .flatMap(ModuleDescriptor::provisionBindings)
                    .map(Binding::key)
            val scopedDependencies = descriptor.modules
                    .flatMap(ModuleDescriptor::dependencies)
                    .filter { it.scope != null }
                    .map(Dependency::key)
            val methods = bindings + scopedDependencies
            methods.map { key ->
                val type = key.type
                val element = MoreTypes.asElement(type)
                val name = element.simpleName.toString().decapitalize()
                val param = ParameterSpec.builder(ClassName.get(type), name).run {
                    key.qualifier?.let {
                        addAnnotation(AnnotationSpec.get(it))
                    }
                    build()
                }
                MethodSpec.methodBuilder(name).run {
                    addModifiers(PUBLIC, ABSTRACT)
                    addAnnotation(BindsInstance::class.java)
                    addParameter(param)
                    build()
                }
            }.map(this::addMethod)
        }
    }
}
