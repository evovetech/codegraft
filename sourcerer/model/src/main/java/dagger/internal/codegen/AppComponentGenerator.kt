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

package dagger.internal.codegen

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeName
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.internal.codegen.ComponentStep.Option.Package
import dagger.model.Key
import org.jetbrains.annotations.Nullable
import sourcerer.JavaOutput
import sourcerer.addAnnotation
import sourcerer.addTo
import sourcerer.classBuilder
import sourcerer.inject.BootScope
import sourcerer.interfaceBuilder
import sourcerer.name
import sourcerer.processor.Env
import sourcerer.toKlass
import sourcerer.typeSpec
import javax.inject.Inject
import javax.inject.Singleton
import javax.lang.model.element.Modifier.ABSTRACT
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.STATIC

internal
class AppComponentGenerator(
    generatedComponents: List<ComponentOutput>,
    storedComponents: List<ComponentOutput>,
    private val pkg: String
) {
    val name = "AppComponent"
    val components = generatedComponents + storedComponents

    fun process(): List<sourcerer.Output> {
        val bootData = BootData()
        val app = App(bootData)
        val bootModule = BootModule(app, bootData)
        val bootComponent = BootComponent(app, bootModule)
        return listOf(
            bootData,
            app,
            bootModule,
            bootComponent
        )
    }

    inner
    class BootData : JavaOutput(
        rawType = ClassName.get(pkg, "${name}_BootData")
    ) {

        override
        fun newBuilder() = outKlass.classBuilder()

        override
        fun typeSpec() = typeSpec {
            val modules = components.map { it.module }
            addAnnotation(ClassName.get(Module::class.java).toKlass()) {
                modules.map { it.outKlass.rawType }
                        .forEach(addTo("includes"))
            }

            val constructor = MethodSpec.constructorBuilder()
                    .addAnnotation(Inject::class.java)
            val bootMethods = components
                    .map { it.bootData }
                    .map { boot ->
                        val type = boot.outKlass.rawType
                        val fieldSpec = addFieldSpec(type, type.name.decapitalize())
                        val getter = MethodSpec.methodBuilder("get${fieldSpec.name.capitalize()}").run {
                            addAnnotation(Provides::class.java)
                            addStatement("return \$N", fieldSpec)
                            returns(type)
                            build()
                        }
                        addMethod(getter)
                        constructor.addToConstructor(fieldSpec)
                        boot.scopedKeys.map { key ->
                            BootMethod(key, fieldSpec)
                        }
                    }
                    .flatMap { it }
                    .groupBy { it.key }
                    .mapValues { it.value.first() }
                    .values
                    .map { (key, fieldSpec) ->
                        key.getterMethod {
                            addAnnotation(Provides::class.java)
                            addAnnotation(Singleton::class.java)
                            addStatement("return \$N.\$L()", fieldSpec, key.getterMethodName)
                        }
                    }

            addMethods(bootMethods.buildUnique())
            addMethod(constructor.build())
        }
    }

    data
    class BootMethod(
        val key: Key,
        val fieldSpec: FieldSpec
    )

    inner
    class App(
        private val bootData: BootData
    ) : JavaOutput(
        rawType = ClassName.get(pkg, name)
    ) {
        val descriptors = components.map { it.descriptor }
        val builder = Builder()

        override
        fun newBuilder() = outKlass.interfaceBuilder()

        override
        fun typeSpec() = typeSpec {
            addModifiers(PUBLIC)
            addAnnotation(Singleton::class.java)
            addAnnotation(ClassName.get(Component::class.java).toKlass()) {
                val add = addTo("modules")
                add(bootData.outKlass.rawType)
            }
            descriptors.forEach {
                val type = it.definitionType
                val name = type.simpleName.toString().decapitalize()
                val getter = MethodSpec.methodBuilder("get${name.capitalize()}")
                        .addModifiers(PUBLIC, ABSTRACT)
                        .returns(TypeName.get(type.asType()))
                        .build()
                addMethod(getter)
            }
            addType(builder.typeSpec())
        }

        inner
        class Builder : sourcerer.JavaOutput.Builder() {
            override
            fun typeSpec() = typeSpec {
                val parent = this@App

                addModifiers(PUBLIC, STATIC)
                addAnnotation(Component.Builder::class.java)

                addMethod(MethodSpec.methodBuilder("bootData").run {
                    addModifiers(PUBLIC, ABSTRACT)
                    addParameter(ParameterSpec.builder(bootData.outKlass.rawType, "bootData").build())
                    returns(outKlass.rawType)
                    build()
                })

                addMethod(MethodSpec.methodBuilder("build").run {
                    addModifiers(PUBLIC, ABSTRACT)
                    returns(parent.outKlass.rawType)
                    build()
                })
            }
        }
    }

    inner
    class BootModule(
        private val app: App,
        private val bootData: BootData
    ) : JavaOutput(
        rawType = ClassName.get(pkg, "BootModule")
    ) {
        override
        fun newBuilder() = outKlass.classBuilder()

        override
        fun typeSpec() = typeSpec {
            // TODO:
            addModifiers(FINAL)
            val modules = components.flatMap { it.descriptor.modules }
            addAnnotation(ClassName.get(Module::class.java).toKlass()) {
                modules.map { TypeName.get(it.moduleElement().asType()) }
                        .forEach(addTo("includes"))
            }
            val componentType = app.outKlass.rawType
            val daggerComponentType = ClassName.get(componentType.packageName(), "Dagger${componentType.name}")
            val bootDataType = bootData.outKlass.rawType
            addMethod(MethodSpec.methodBuilder("provideComponent").run {
                addAnnotation(Provides::class.java)
                addAnnotation(BootScope::class.java)

                val bootDataParam = ParameterSpec.builder(bootDataType, "bootData").build()
                addParameter(bootDataParam)
                // TODO: add module parameters
                addStatement(CodeBlock.builder().run {
                    //                    add("return null")
                    add("return \$T.builder()\n", daggerComponentType)
                    indent()
                    add(".bootData(\$N)\n", bootDataParam)
                    add(".build()")
                    unindent()
                    build()
                })
                returns(componentType)
                build()
            })
        }
    }

    inner
    class BootComponent(
        private val app: App,
        private val bootModule: BootModule
    ) : JavaOutput(
        rawType = ClassName.get(pkg, "BootComponent")
    ) {
        val descriptors = components.map { it.descriptor }
        val builder = Builder()

        override
        fun newBuilder() = outKlass.interfaceBuilder()

        override
        fun typeSpec() = typeSpec {
            addModifiers(PUBLIC, STATIC)
            addAnnotation(BootScope::class.java)
            addAnnotation(ClassName.get(Component::class.java).toKlass()) {
                val add = addTo("modules")
                add(bootModule.outKlass.rawType)
            }

            // appcomponent
            val type = app.outKlass.rawType
            addMethod(MethodBuilder("get${app.outKlass.name.capitalize()}") {
                addModifiers(PUBLIC, ABSTRACT)
                returns(type)
            }.build())

            addType(builder.typeSpec())
        }

        inner
        class Builder : sourcerer.JavaOutput.Builder() {
            override
            fun typeSpec() = typeSpec {
                addModifiers(PUBLIC, STATIC)
                addAnnotation(Component.Builder::class.java)

                val applicationModules = descriptors
                        .flatMap { it.applicationModules }
                        .map { module ->
                            val param = module.moduleElement().buildParameter {
                                addAnnotation(Nullable::class.java)
                            }
                            MethodBuilder(param.name) {
                                addAnnotation(BindsInstance::class.java)
                                addModifiers(PUBLIC, ABSTRACT)
                                addParameter(param)
                                returns(outKlass.rawType)
                            }
                        }

                val dependencies = descriptors
                        .flatMap { it.modules }
                        .flatMap { it.bindings() }
                        .groupBy { it.key() }
                        .mapValues { it.value.first() }
                        .values
                val dependencyMethods = dependencies.map { dep ->
                    val param = dep.buildParameter()
                    MethodBuilder(param.name) {
                        addAnnotation(BindsInstance::class.java)
                        addModifiers(PUBLIC, ABSTRACT)
                        addParameter(param)
                        returns(outKlass.rawType)
                    }
                }

                (applicationModules + dependencyMethods)
                        .buildUnique()
                        .map(this::addMethod)

                val parent = this@BootComponent

                addMethod(MethodSpec.methodBuilder("build").run {
                    addModifiers(PUBLIC, ABSTRACT)
                    returns(parent.outKlass.rawType)
                    build()
                })
            }
        }
    }

    class Factory
    @Inject constructor(
        private val options: Env.Options
    ) {
        fun create(
            generatedComponents: List<ComponentOutput>,
            storedComponents: List<ComponentOutput>
        ) = AppComponentGenerator(
            generatedComponents = generatedComponents,
            storedComponents = storedComponents,
            pkg = options[Package]
        )
    }
}
