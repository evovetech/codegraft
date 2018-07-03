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
import sourcerer.addAnnotation
import sourcerer.addMethod
import sourcerer.addTo
import sourcerer.interfaceBuilder
import sourcerer.name
import sourcerer.typeSpec
import javax.inject.Inject
import javax.lang.model.element.Modifier

class AndroidInjectModuleGenerator(
    private val descriptor: AndroidInjectModuleDescriptor
) : JavaOutput(
    rawType = ClassName.get(descriptor.element),
    outExt = "Module"
) {
    override
    fun newBuilder() = outKlass.interfaceBuilder()

    override
    fun typeSpec() = typeSpec {
        addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
        addAnnotation(Dagger.Module) {
            descriptor.includes
                    .map(ClassName::get)
                    .map(addTo("includes"))
        }
        addMethod("contribute${rawType.name}") {
            addAnnotation(Codegen.Inject.ActivityScope.rawType)
            addAnnotation(Dagger.Android.ContributesInjector.rawType)
            addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            returns(rawType)
        }
    }

    class Factory
    @Inject constructor() {
        fun create(
            descriptor: AndroidInjectModuleDescriptor
        ): AndroidInjectModuleGenerator {
            return AndroidInjectModuleGenerator(descriptor)
        }
    }
}
