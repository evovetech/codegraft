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

package dagger.internal.codegen

import codegraft.android.AndroidInjectModuleDescriptor
import codegraft.android.AndroidInjectModuleDescriptor.Kind
import codegraft.android.AndroidInjectModuleGenerator
import codegraft.bootstrap.ComponentOutput
import codegraft.bootstrap.Package
import codegraft.bootstrap.buildParameter
import codegraft.bootstrap.equality
import codegraft.bootstrap.getterMethod
import codegraft.bootstrap.getterMethodName
import codegraft.bootstrap.key
import codegraft.inject.BootScope
import codegraft.plugins.GeneratePluginBindingsModuleDescriptor
import com.google.auto.common.MoreElements.hasModifiers
import com.google.common.collect.ImmutableSet
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
import dagger.internal.codegen.BootstrapComponentDescriptor.Modules
import dagger.model.Key
import org.jetbrains.annotations.Nullable
import sourcerer.Includable
import sourcerer.JavaOutput
import sourcerer.MethodBuilder
import sourcerer.Output
import sourcerer.addAnnotation
import sourcerer.addFieldSpec
import sourcerer.addTo
import sourcerer.addToConstructor
import sourcerer.buildUnique
import sourcerer.buildUniquePairs
import sourcerer.classBuilder
import sourcerer.getFieldName
import sourcerer.interfaceBuilder
import sourcerer.name
import sourcerer.nestedBuilder
import sourcerer.processor.ProcessingEnv
import sourcerer.toImmutableSet
import sourcerer.toKlass
import sourcerer.typeSpec
import javax.inject.Inject
import javax.inject.Singleton
import javax.lang.model.element.Modifier.ABSTRACT
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.STATIC
import javax.lang.model.element.TypeElement
import kotlin.reflect.KClass

internal
class AppComponentGenerator(
    val pluginModuleDescriptors: ImmutableSet<GeneratePluginBindingsModuleDescriptor>,
    val pluginModules: ImmutableSet<GeneratePluginBindingsModuleDescriptor.Out>,
    val injectModuleDescriptors: ImmutableSet<AndroidInjectModuleDescriptor>,
    val injectModules: ImmutableSet<AndroidInjectModuleGenerator>,
    val componentDescriptors: ImmutableSet<BootstrapComponentDescriptor>,
    val components: ImmutableSet<ComponentOutput>,
    private val pkg: String
) {
    val name = "AppComponent"
    val nestedComponents = components.filterNot { it.descriptor.flatten }
            .toImmutableSet()
    val flatComponents = components.filter { it.descriptor.flatten }
            .toImmutableSet()

    fun process(): List<Output> {
        // TODO: verify correct logic
        if (components.isEmpty()) {
            return emptyList()
        }
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
            val modules = components.flatMap { output ->
                val module = output.module
                if (module.include) {
                    listOf(module.outKlass.rawType)
                } else {
                    output.descriptor.applicationModules
                            .map(ModuleDescriptor::moduleElement)
                            .map(ClassName::get)
                }
            }
            addAnnotation(ClassName.get(Module::class.java).toKlass()) {
                modules.forEach(addTo("includes"))
            }

            val constructor = MethodSpec.constructorBuilder()
                    .addAnnotation(Inject::class.java)
            val bootMethods = components
                    .map { it.bootData }
                    .filter(Includable::include)
                    .map { boot ->
                        val type = boot.outKlass.rawType
                        val fieldSpec = addFieldSpec(type, type.name.decapitalize())
//                        val getter = MethodSpec.methodBuilder("get${fieldSpec.name.capitalize()}").run {
//                            addAnnotation(Provides::class.java)
//                            addStatement("return \$N", fieldSpec)
//                            returns(type)
//                            build()
//                        }
//                        addMethod(getter)
                        constructor.addToConstructor(fieldSpec)
                        boot.scopedKeys.map { key ->
                            BootMethod(key, fieldSpec)
                        }
                    }
                    .flatMap { it }
                    .groupBy { it.key.equality }
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
        private
        val descriptors = components.map(ComponentOutput::descriptor)
        private
        val modules = descriptors
                .map(BootstrapComponentDescriptor::applicationModules)
                .flatMap(Modules::transitiveModules)
                .filterNot { hasModifiers<TypeElement>(ABSTRACT).apply(it.moduleElement()) }
                .toImmutableSet()
        internal
        val builder = Builder(modules)

        override
        fun newBuilder() = outKlass.interfaceBuilder()

        override
        fun typeSpec() = typeSpec {
            addModifiers(PUBLIC)
            addAnnotation(Singleton::class.java)
            addAnnotation(ClassName.get(Component::class.java).toKlass()) {
                val add = addTo("modules")
                add(bootData.outKlass.rawType)

                // injects
                injectModules.map { it.outKlass.rawType }
                        .map(add)

                // plugins
                pluginModules.map { it.outKlass.rawType }
                        .map(add)
            }
            descriptors.filter { it.flatten }.forEach { descriptor ->
                addSuperinterface(TypeName.get(descriptor.componentDefinitionType.asType()))
                descriptor.componentMethods.map {
                    MethodSpec.overriding(it.methodElement)
                            .addModifiers(ABSTRACT)
                            .build()
                }.map(this::addMethod)
            }
            descriptors.filterNot { it.flatten }.forEach { descriptor ->
                val type = descriptor.componentDefinitionType
                val name = type.asType().getFieldName()
                val getter = MethodSpec.methodBuilder("get${name.capitalize()}")
                        .addModifiers(PUBLIC, ABSTRACT)
                        .returns(TypeName.get(type.asType()))
                        .build()
                addMethod(getter)
            }
            addType(builder.typeSpec())
        }

        inner
        class Builder(
            val modules: ImmutableSet<ModuleDescriptor>
        ) : JavaOutput.Builder() {
            private
            val bootMethodBuilder = MethodBuilder("bootData") {
                addModifiers(PUBLIC, ABSTRACT)
                addParameter(ParameterSpec.builder(bootData.outKlass.rawType, "bootData").build())
                returns(outKlass.rawType)
            }.let {
                Pair(it, false)
            }
            private
            val moduleMethodBuilders = modules.map { module ->
                val element = module.moduleElement()
                val name = element.asType().getFieldName()
                val type = TypeName.get(element.asType())
                MethodBuilder(name) {
                    addModifiers(PUBLIC, ABSTRACT)
                    addParameter(ParameterSpec.builder(type, name).build())
                    returns(outKlass.rawType)
                }
            }.map {
                Pair(it, true)
            }
            val setMethods = (moduleMethodBuilders + bootMethodBuilder)
                    .buildUniquePairs()
            val bootMethod = setMethods.filterNot { it.second }
                    .map { it.first }
            val moduleMethods = setMethods.filter { it.second }
                    .map { it.first }
            val buildMethod = MethodBuilder("build") {
                addModifiers(PUBLIC, ABSTRACT)
                returns(this@App.outKlass.rawType)
            }

            override
            fun typeSpec() = typeSpec {
                addModifiers(PUBLIC, STATIC)
                addAnnotation(Component.Builder::class.java)
                addMethods(setMethods.map(Pair<MethodSpec, *>::first))
                addMethod(buildMethod.build())
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
        val componentType = app.outKlass.rawType
        val daggerComponentType = ClassName.get(componentType.packageName(), "Dagger${componentType.name}")
        val bootDataType = bootData.outKlass.rawType
        val builderType = componentType.nestedBuilder()

        val builderName = "builder"
        val mapper: (MethodSpec, Boolean) -> Pair<ParameterSpec, CodeBlock> = { spec, nullable ->
            var param = spec.parameters.first()
            val paramName = param.name
            val methodName = spec.name
            val codeBlock = CodeBlock.builder()
            val statement = CodeBlock.of("\$N.\$N(\$N)", builderName, methodName, paramName)
            if (nullable) {
                codeBlock.beginControlFlow("if (\$N != null)", paramName)
                        .addStatement(statement)
                        .endControlFlow()
                param = param.toBuilder()
                        .addAnnotation(Nullable::class.java)
                        .build()
            } else {
                codeBlock.addStatement(statement)
            }
            Pair(param, codeBlock.build())
        }
        val moduleMethods = app.builder.moduleMethods.map {
            mapper(it, true)
        }
        val bootMethod = app.builder.bootMethod.map {
            mapper(it, false)
        }
        val builderMethods = moduleMethods + bootMethod

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

            addMethod(MethodSpec.methodBuilder("provideComponent").run {
                addAnnotation(Provides::class.java)
                addAnnotation(BootScope::class.java)
                addParameters(builderMethods.map { it.first })
                addStatement("\$T \$N = \$T.builder()", builderType, builderName, daggerComponentType)
                builderMethods.map { it.second }
                        .map(this::addCode)
                addStatement("return \$N.build()", builderName)
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
        class Builder : JavaOutput.Builder() {
            override
            fun typeSpec() = typeSpec {
                addModifiers(PUBLIC, STATIC)
                addAnnotation(Component.Builder::class.java)

                val applicationModules = bootModule.moduleMethods.map { (param, codeblock) ->
                    MethodBuilder(param.name) {
                        addAnnotation(BindsInstance::class.java)
                        addModifiers(PUBLIC, ABSTRACT)
                        addParameter(param)
                        returns(outKlass.rawType)
                    }
                }
                val bootstrapModules = descriptors.flatMap { it.modules }
                        .filterNot { module -> hasModifiers<TypeElement>(ABSTRACT).apply(module.moduleElement()) }
                        .map { module ->
                            val name = module.moduleElement().asType().getFieldName()
                            val type = TypeName.get(module.moduleElement().asType())
                            val param = ParameterSpec.builder(type, name).build()
                            MethodBuilder(name) {
                                addModifiers(PUBLIC, ABSTRACT)
                                addParameter(param)
                                returns(outKlass.rawType)
                            }
                        }

                val dependencies = descriptors
                        .flatMap { it.modules }
                        .flatMap { it.dependencies }
                        .groupBy { it.key.equality }
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

                (dependencyMethods + applicationModules + bootstrapModules)
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
        private val options: ProcessingEnv.Options,
        private val componentOutputFactory: ComponentOutput.Factory,
        private val moduleOutputFactory: AndroidInjectModuleGenerator.Factory
    ) {
        init {
            println("\npkg=${options.Package}\n")
        }

        fun create(
            generatedPluginModules: codegraft.plugins.Modules,
            storedPluginModules: codegraft.plugins.Modules,
            generatedModules: ImmutableSet<AndroidInjectModuleDescriptor>,
            storedModules: ImmutableSet<AndroidInjectModuleDescriptor>,
            generatedComponents: ImmutableSet<BootstrapComponentDescriptor>,
            storedComponents: ImmutableSet<BootstrapComponentDescriptor>
        ): AppComponentGenerator {
            val pluginModuleDescriptors = (generatedPluginModules.values() + storedPluginModules.values())
                    .toImmutableSet()
            val pluginModules = pluginModuleDescriptors
                    .map(GeneratePluginBindingsModuleDescriptor::output)
                    .toImmutableSet()

            val injectModuleDescriptors = (generatedModules + storedModules)
                    .toImmutableSet()
            val injectModuleKinds = injectModuleDescriptors
                    .groupBy(AndroidInjectModuleDescriptor::kind)
                    .keys
                    .map(Kind::moduleType)
                    .map(KClass<*>::java)
                    .map(ClassName::get)
                    .toImmutableSet()
            val injectModules = injectModuleDescriptors
                    .map(moduleOutputFactory::create)
                    .toImmutableSet()

            val allComponents = (generatedComponents + storedComponents)
            val componentDescriptors = allComponents
                    .filter { descriptor ->
                        if (descriptor.autoInclude) {
                            true
                        } else {
                            val moduleTypes = descriptor.applicationModules.modules
                                    .map(ModuleDescriptor::moduleElement)
                                    .map(ClassName::get)
                            moduleTypes.any { type ->
                                injectModuleKinds.contains(type)
                            }
                        }
                    }
                    .flatMap { it.allDependencies + it }
                    .toImmutableSet()
            val components = componentDescriptors
                    .map(componentOutputFactory::create)
                    .toImmutableSet()

            return AppComponentGenerator(
                pluginModuleDescriptors = pluginModuleDescriptors,
                pluginModules = pluginModules,
                injectModuleDescriptors = injectModuleDescriptors,
                injectModules = injectModules,
                componentDescriptors = componentDescriptors,
                components = components,
                pkg = options.Package
            )
        }
    }
}
