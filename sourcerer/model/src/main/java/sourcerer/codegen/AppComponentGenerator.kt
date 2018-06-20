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
import sourcerer.JavaOutput
import sourcerer.dev.BootstrapComponentStep.Output
import sourcerer.dev.ComponentStep.Option.Package
import sourcerer.interfaceBuilder
import sourcerer.processor.Env
import sourcerer.typeSpec
import javax.inject.Inject

/*
@Module(includes = [CrashesComponent_Module::class])
class AppComponent_BootData
@Inject constructor(
    @get:Provides
    val crashes: CrashesComponent_BootData
) {
    val app: AndroidApplication
        @Provides @Singleton
        get() = crashes.app

    val fabric: Fabric
        @Provides @Singleton
        get() = crashes.fabric
}

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
    private val actual: Builder
) : Builder by actual {
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
    pkg: String
) : JavaOutput(
    rawType = ClassName.get(pkg, "AppComponent_BootData2")
) {
    override
    fun newBuilder() = outKlass.interfaceBuilder()

    override
    fun typeSpec() = typeSpec {
        //        addModifiers(PUBLIC)
//        addAnnotation(Singleton::class.java)
//        descriptors.map(ComponentDescriptor::definitionType)
//                .map(TypeElement::asType)
//                .map(TypeName::get)
//                .map(this::addSuperinterface)

//        addAnnotation(ClassName.get(ApplicationComponent::class.java).toKlass()) {
//            descriptor.applicationModules
//                    .map(ModuleDescriptor::definitionType)
//                    .mapNotNull(ClassName::get)
//                    .forEach(addTo("modules"))
//        }
//        addType(Builder().typeSpec())
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

//    inner
//    class Builder : SourceWriter {
//        override
//        val outKlass: Klass = "Builder".toKlass()
//
//        override
//        fun newBuilder() = outKlass.interfaceBuilder()
//
//        override
//        fun typeSpec() = typeSpec {
//            addModifiers(PUBLIC, STATIC)
//            addAnnotation(ApplicationComponent.Builder::class.java)
//
//            // TODO: override all methods for clarity
////            ElementFilter.methodsIn()
//
//            // add method for each module
//            descriptor.applicationModules.map { module ->
//                val type = module.definitionType
//                val name = type.simpleName.toString().decapitalize()
//                MethodSpec.methodBuilder(name).run {
//                    addModifiers(PUBLIC, ABSTRACT)
//                    addParameter(ClassName.get(type), name)
//                    build()
//                }
//            }.map(this::addMethod)
//
//            val bindings = descriptor.modules
//                    .flatMap(ModuleDescriptor::provisionBindings)
//                    .map(Binding::key)
//            val scopedDependencies = descriptor.modules
//                    .flatMap(ModuleDescriptor::dependencies)
//                    .filter { it.scope != null }
//                    .map(Dependency::key)
//            val methods = bindings + scopedDependencies
//            methods.map { key ->
//                val type = key.type
//                val element = MoreTypes.asElement(type)
//                val name = element.simpleName.toString().decapitalize()
//                val param = ParameterSpec.builder(ClassName.get(type), name).run {
//                    key.qualifier?.let {
//                        addAnnotation(AnnotationSpec.get(it))
//                    }
//                    build()
//                }
//                MethodSpec.methodBuilder(name).run {
//                    addModifiers(PUBLIC, ABSTRACT)
//                    addAnnotation(BindsInstance::class.java)
//                    addParameter(param)
//                    build()
//                }
//            }.map(this::addMethod)
//        }
//    }
}
