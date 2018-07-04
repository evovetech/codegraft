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

package sourcerer.bootstrap

import com.squareup.javapoet.ClassName
import sourcerer.Codegen
import sourcerer.Dagger
import sourcerer.JavaOutput
import sourcerer.Klass
import sourcerer.SourceWriter
import sourcerer.addAnnotation
import sourcerer.addMethod
import sourcerer.addTo
import sourcerer.bootstrap.AndroidInjectModuleDescriptor.Kind.Application
import sourcerer.interfaceBuilder
import sourcerer.name
import sourcerer.toKlass
import sourcerer.typeSpec
import javax.inject.Inject
import javax.lang.model.element.Modifier.ABSTRACT
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.STATIC

class AndroidInjectModuleGenerator(
    private val descriptor: AndroidInjectModuleDescriptor
) : JavaOutput(
    rawType = ClassName.get(descriptor.element),
    outExt = "Module"
) {
    override
    fun newBuilder() = outKlass.interfaceBuilder()

    override
    fun typeSpec() = when (descriptor.kind) {
        Application -> applicationTypeSpec()
        else -> normalTypeSpec()
    }

    /*

@Module(
    subcomponents = [MainApplicationSubcomponent::class]
)
interface MainApplicationModule {
    @Binds
    @IntoMap
    @ApplicationKey(App::class)
    fun bindAndroidInjectorFactory(
        builder: MainApplicationSubcomponent.Builder
    ): AndroidInjector.Factory<out Application>

    @Subcomponent
    interface MainApplicationSubcomponent : AndroidInjector<App> {
        @Subcomponent.Builder
        abstract class Builder : AndroidInjector.Builder<App>()
    }
}

     */
    private
    fun applicationTypeSpec() = typeSpec {
        addModifiers(PUBLIC, STATIC)

        val subcomponent = Subcomponent(this@AndroidInjectModuleGenerator)

        addAnnotation(Dagger.Module) {
            subcomponent.fullType
                    .let(addTo("subcomponents"))
            val addToIncludes = addTo("includes")
            descriptor.includes
                    .map(ClassName::get)
                    .map(addToIncludes)
            descriptor.kind.moduleType.java
                    .let(ClassName::get)
                    .let(addToIncludes)
        }

        addType(subcomponent.typeSpec())
    }

    private fun normalTypeSpec() = typeSpec {
        addModifiers(PUBLIC, STATIC)
        addAnnotation(Dagger.Module) {
            val addToIncludes = addTo("includes")
            descriptor.includes
                    .map(ClassName::get)
                    .map(addToIncludes)
            descriptor.kind.moduleType.java
                    .let(ClassName::get)
                    .let(addToIncludes)
        }
        addMethod("contribute${rawType.name}") {
            addAnnotation(Codegen.Inject.ActivityScope.rawType)
            addAnnotation(Dagger.Android.ContributesInjector.rawType)
            addModifiers(PUBLIC, ABSTRACT)
            returns(rawType)
        }
    }

    class Subcomponent(
        val parent: AndroidInjectModuleGenerator
    ) : SourceWriter {
        override
        val outKlass: Klass = "Subcomponent".toKlass()
        val fullType = parent.outKlass.rawType.nestedClass(outKlass.name)
        val builder = SubcomponentBuilder(this)

        override
        fun newBuilder() = outKlass.interfaceBuilder()

        override
        fun typeSpec() = typeSpec {
            addModifiers(PUBLIC, STATIC)
            addAnnotation(dagger.Subcomponent::class.java)
            addType(builder.typeSpec())
        }
    }

    class SubcomponentBuilder(
        val parent: Subcomponent
    ) : sourcerer.JavaOutput.Builder() {
        val fullType = parent.fullType.nestedClass(outKlass.name)

        override
        fun typeSpec() = typeSpec {
            addModifiers(PUBLIC, STATIC)
            addAnnotation(dagger.Subcomponent.Builder::class.java)

            addMethod("build") {
                addModifiers(PUBLIC, ABSTRACT)
                returns(parent.fullType)
            }
        }
    }

    class Factory
    @Inject constructor(

    ) {
        fun create(
            descriptor: AndroidInjectModuleDescriptor
        ): AndroidInjectModuleGenerator {
            return AndroidInjectModuleGenerator(descriptor)
        }
    }
}
