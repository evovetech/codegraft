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

package sourcerer.codegen

import com.google.auto.common.MoreElements
import com.google.auto.common.MoreTypes
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeName
import dagger.BindsInstance
import sourcerer.Env
import sourcerer.JavaOutput
import sourcerer.Klass
import sourcerer.SourceWriter
import sourcerer.addAnnotation
import sourcerer.addTo
import sourcerer.dev.Binding
import sourcerer.dev.ComponentDescriptor
import sourcerer.dev.Dependency
import sourcerer.dev.ModuleDescriptor
import sourcerer.dev.SourcererElements
import sourcerer.inject.ApplicationComponent
import sourcerer.interfaceBuilder
import sourcerer.toKlass
import sourcerer.typeSpec
import javax.inject.Inject
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier.ABSTRACT
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.STATIC
import javax.lang.model.util.ElementFilter.methodsIn

class ApplicationComponentGenerator(
    private val env: Env,
    private val elements: SourcererElements,
    private val descriptor: ComponentDescriptor
) : JavaOutput(
    rawType = ClassName.get(descriptor.definitionType),
    outExt = "ApplicationComponent"
) {
    override
    fun newBuilder() = outKlass.interfaceBuilder()

    override
    fun typeSpec() = typeSpec {
        addModifiers(PUBLIC)
        addSuperinterface(TypeName.get(descriptor.definitionType.asType()))
        methodsIn(elements.getAllMembers(descriptor.definitionType))
                .filter(MoreElements.hasModifiers<ExecutableElement>(ABSTRACT)::apply)
                .map(MethodSpec::overriding)
                .map {
                    it.addModifiers(ABSTRACT)
                            .build()
                }
//                .map(MethodSpec.Builder::build)
                .apply { env.log("methods=$this") }
                .map(this::addMethod)
        addAnnotation(ClassName.get(ApplicationComponent::class.java).toKlass()) {
            descriptor.applicationModules
                    .map(ModuleDescriptor::definitionType)
                    .mapNotNull(ClassName::get)
                    .forEach(addTo("modules"))
        }
        addType(Builder().typeSpec())
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
            descriptor.applicationModules
                    .filterNot { it.definitionType.modifiers.contains(ABSTRACT) }
                    .map { module ->
                        val type = module.definitionType
                        val name = type.simpleName.toString().decapitalize()
                        MethodSpec.methodBuilder(name).run {
                            addModifiers(PUBLIC, ABSTRACT)
                            addParameter(ClassName.get(type), name)
                            build()
                        }
                    }
                    .map(this::addMethod)

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

    class Factory
    @Inject constructor(
        private val env: Env,
        private val elements: SourcererElements
    ) {
        fun create(
            descriptor: ComponentDescriptor
        ) = ApplicationComponentGenerator(
            env = env,
            elements = elements,
            descriptor = descriptor
        )
    }
}
