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

package codegraft.bootstrap

import codegraft.inject.GeneratePluginBindings
import com.google.auto.common.MoreElements
import com.google.common.collect.ImmutableSet
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import sourcerer.AnnotationElements
import sourcerer.AnnotationStep
import sourcerer.AnnotationType
import sourcerer.JavaOutput
import sourcerer.Output
import sourcerer.Outputs
import sourcerer.addTo
import sourcerer.getFieldName
import sourcerer.interfaceBuilder
import sourcerer.processor.ProcessingEnv
import sourcerer.toImmutableSet
import sourcerer.typeInputs
import sourcerer.typeSpec
import javax.annotation.processing.RoundEnvironment
import javax.inject.Inject
import javax.inject.Singleton
import javax.lang.model.element.Modifier.ABSTRACT
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.STATIC
import javax.lang.model.element.TypeElement

@Singleton
class GeneratePluginBindingsStep
@Inject constructor(
    val descriptorFactory: GeneratePluginBindingsDescriptor.Factory,
    val outputFactory: GeneratePluginBindingsGenerator.Factory,
    val sourcerer: GeneratePluginBindingsSourcerer
) : AnnotationStep() {
    private
    var _plugins: Set<GeneratePluginBindingsDescriptor> = LinkedHashSet()

    val plugins: ImmutableSet<GeneratePluginBindingsDescriptor>
        get() = _plugins.toImmutableSet()

    val storedPlugins: ImmutableSet<GeneratePluginBindingsDescriptor> by lazy {
        sourcerer.storedOutputs()
                .map(descriptorFactory::forStoredModule)
                .toImmutableSet()
    }

    fun sourcererOutput(): Output {
        return sourcerer.output(plugins)
    }

    override
    fun ProcessingEnv.annotations(): Set<AnnotationType> = setOf(
        GeneratePluginBindings::class
    )

    override
    fun ProcessingEnv.process(annotationElements: AnnotationElements): Outputs {
        val injections = annotationElements.typeInputs<GeneratePluginBindings>()
                .map { it.element }
        val descriptors = injections
                .map(descriptorFactory::create)
                .toImmutableSet()
        _plugins += descriptors
        return descriptors
                .map(outputFactory::create)
                .flatMap(GeneratePluginBindingsGenerator::process)
    }

    override
    fun postRound(roundEnv: RoundEnvironment): Outputs {
        val allPlugins = plugins + storedPlugins

        val maps2: Map<GeneratePluginBindingsDescriptor, List<TypeElement>> = allPlugins.associate { descriptor ->
            val key = descriptor.annotationType
            val values = roundEnv.getElementsAnnotatedWith(key)
                    .map(MoreElements::asType)
            Pair(descriptor, values)
        }
        maps2.forEach { k, v ->
            getEnv().log("plugin entry: ${k.annotationType}=$v")
        }

        return maps2.flatMap { (descriptor, elements) ->
            elements.map { element ->
                GeneratedPluginModule(descriptor, element)
            }
        }
    }
}

/*
    @Module
    interface DaggerModule {
        @Binds
        @IntoMap
        @ViewModelKey(PlaidViewModel::class)
        fun bindViewModel(viewModel: PlaidViewModel): ViewModel
    }
 */
class GeneratedPluginModule(
    val descriptor: GeneratePluginBindingsDescriptor,
    val element: TypeElement
) : JavaOutput(
    rawType = ClassName.get(element),
    outExt = "Module"
) {
    override
    fun newBuilder() =
        outKlass.interfaceBuilder()

    override
    fun typeSpec() = typeSpec {
        addModifiers(PUBLIC, STATIC)
        addAnnotation(Module::class.java)

        val returnType = descriptor.pluginType.className
        val paramType = element.className
        val param = ParameterSpec.builder(paramType, paramType.getFieldName()).run {
            // TODO:
            build()
        }

        val pluginTypeName = descriptor.pluginTypeName
        addMethod(MethodSpec.methodBuilder("bind$pluginTypeName").run {
            addModifiers(PUBLIC, ABSTRACT)

            addAnnotation(Binds::class.java)
            addAnnotation(IntoMap::class.java)
            addAnnotation(AnnotationSpec.builder(descriptor.mapKeyAnnotationType).run {
                paramType.let(addTo("value"))
                build()
            })

            addParameter(param)
            returns(returnType)
            build()
        })
    }
}
