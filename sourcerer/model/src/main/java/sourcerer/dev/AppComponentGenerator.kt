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

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import sourcerer.JavaOutput
import sourcerer.interfaceBuilder
import sourcerer.processor.Env
import sourcerer.typeSpec
import javax.inject.Inject
import javax.inject.Singleton
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.TypeElement

class AppComponentGenerator(
    private val descriptors: List<ComponentDescriptor>,
    pkg: String
) : JavaOutput(
    rawType = ClassName.get(pkg, "AppComponent2")
) {
    override
    fun newBuilder() = outKlass.interfaceBuilder()

    override
    fun typeSpec() = typeSpec {
        addModifiers(PUBLIC)
        addAnnotation(Singleton::class.java)
        descriptors.map(ComponentDescriptor::definitionType)
                .map(TypeElement::asType)
                .map(TypeName::get)
                .map(this::addSuperinterface)

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
            descriptors: List<ComponentDescriptor>
        ) = AppComponentGenerator(
            descriptors = descriptors,
            pkg = options[ComponentStep.Option.Package]
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
