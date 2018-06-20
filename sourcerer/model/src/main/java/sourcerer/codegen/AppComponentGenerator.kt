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

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeName
import dagger.Component
import dagger.Module
import dagger.Provides
import sourcerer.JavaOutput
import sourcerer.addAnnotation
import sourcerer.addTo
import sourcerer.classBuilder
import sourcerer.dev.BootstrapComponentStep.Output
import sourcerer.dev.ComponentStep.Option.Package
import sourcerer.interfaceBuilder
import sourcerer.name
import sourcerer.processor.Env
import sourcerer.toKlass
import sourcerer.typeSpec
import javax.inject.Inject
import javax.inject.Singleton
import javax.lang.model.element.Modifier.ABSTRACT
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.STATIC

/*

@Singleton
@Component(modules = [AppComponent_BootData::class])
interface AppComponent {
    val crashesComponent: CrashesComponent

    @Component.Builder
    interface Builder {
        fun bootData(bootData: AppComponent_BootData)
        fun crashes(crashes: Crashes)
        fun build(): AppComponent
    }
}

class AppComponent_Builder
private constructor(
    private val actual: AppComponent.Builder
) : AppComponent.Builder by actual {
    @Inject constructor(
        bootData: AppComponent_BootData,
        crashes: Crashes?
    ) : this(
        actual = DaggerAppComponent.builder()
    ) {
        actual.bootData(bootData)
        crashes?.let {
            actual.crashes(it)
        }
    }
}
 */

class AppComponentGenerator(
    private val descriptors: List<Output>,
    private val pkg: String
) {
    fun process(): List<sourcerer.Output> {
        val bootData = BootData(this)
        val app = App(bootData)
        return listOf(
            bootData,
            app
        )
    }

    /*
    @Singleton
    @Component(modules = [AppComponent_BootData::class])
    interface AppComponent {
        val crashesComponent: CrashesComponent

        @Component.Builder
        interface Builder {
            fun bootData(bootData: AppComponent_BootData)
            fun crashes(crashes: Crashes)
            fun build(): AppComponent
        }
    }
    */
    class App(
        private val bootData: BootData
    ) : JavaOutput(
        rawType = ClassName.get(bootData.pkg.name, "AppComponent2")
    ) {
        val components = bootData.components
        val descriptors = components.map { it.descriptor }

        override
        fun newBuilder() = outKlass.interfaceBuilder()

        override
        fun typeSpec() = typeSpec {
            addAnnotation(Singleton::class.java)
//            addAnnotation(ClassName.get(Component::class.java).toKlass()) {
//                val add = addTo("modules")
//                add(bootData.outKlass.rawType)
//            }
            descriptors.forEach {
                val type = it.definitionType
                val name = type.simpleName.toString().decapitalize()
                val getter = MethodSpec.methodBuilder("get${name.capitalize()}")
                        .addModifiers(PUBLIC, ABSTRACT)
                        .returns(TypeName.get(type.asType()))
                        .build()
                addMethod(getter)
            }
            addType(Builder().typeSpec())
        }

        inner
        class Builder : sourcerer.JavaOutput.Builder() {
            override
            fun typeSpec() = typeSpec {
                val parent = this@App

                addModifiers(PUBLIC, STATIC)
//                addAnnotation(Component.Builder::class.java)

                addMethod(MethodSpec.methodBuilder("bootData").run {
                    addModifiers(PUBLIC, ABSTRACT)
                    addParameter(ParameterSpec.builder(bootData.outKlass.rawType, "bootData").build())
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

    class BootData(
        parent: AppComponentGenerator
    ) : JavaOutput(
        rawType = ClassName.get(parent.pkg, "AppComponent_BootData2")
    ) {
        val descriptors = parent.descriptors
        val components = descriptors.map { it.component }

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
            val boots = components.map { it.bootData }
            boots.forEach { boot ->
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

                val typeSpec = boot.typeSpec()
                val methodSpecs = typeSpec.methodSpecs.filterNot(MethodSpec::isConstructor)
                val methods = methodSpecs.map { method ->
                    val name = method.name
                    MethodSpec.methodBuilder(name)
                            .addAnnotation(Provides::class.java)
                            .addAnnotation(Singleton::class.java)
                            .returns(method.returnType)
                            .addStatement("return \$N.\$L()", fieldSpec, name)
                            .build()
                }
                addMethods(methods)
            }
            addMethod(constructor.build())
        }
    }

    class Factory
    @Inject constructor(
        private val options: Env.Options
    ) {
        fun create(
            descriptors: List<Output>
        ) = AppComponentGenerator(
            descriptors = descriptors,
            pkg = options[Package]
        )
    }
}
