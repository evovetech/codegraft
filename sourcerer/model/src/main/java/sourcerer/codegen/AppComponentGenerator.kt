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
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.STATIC

class AppComponentGenerator(
    private val descriptors: List<Output>,
    private val pkg: String
) {
    val name = "AppComponent2"
    val components = descriptors.map { it.component }

    fun process(): List<sourcerer.Output> {
        val bootData = BootData()
        val app = App(bootData)
        val appBuilder = AppBuilder(app)
        return listOf(
            bootData,
            app,
            appBuilder
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
            addType(builder.typeSpec())
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

    /*
    class AppComponent_Builder
    private constructor(
        private val actual: Builder
    ) {
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

        fun build(): AppComponent {
            return actual.build()
        }
    }
     */
    inner
    class AppBuilder(
        private val app: App
    ) : JavaOutput(
        rawType = ClassName.get(pkg, "${name}_Builder")
    ) {
        override
        fun newBuilder() = outKlass.classBuilder()

        override
        fun typeSpec() = typeSpec {
            // TODO:
            addModifiers(FINAL)

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
