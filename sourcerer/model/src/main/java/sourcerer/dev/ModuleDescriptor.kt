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

import com.google.auto.common.MoreElements
import com.google.auto.common.MoreElements.isAnnotationPresent
import com.google.common.collect.ImmutableList
import dagger.Module
import dagger.Provides
import javax.inject.Inject
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.util.ElementFilter.methodsIn
import javax.lang.model.util.Elements

data
class ModuleDescriptor(
    val definitionType: TypeElement,
    val moduleMirror: AnnotationMirror,
    val providesMethods: ImmutableList<MethodDescriptor>
) {
    class Factory
    @Inject constructor(
        val elements: Elements,
        val types: SourcererTypes,
        val methodFactory: MethodDescriptor.Factory
    ) {
        fun create(
            moduleDefinitionType: TypeElement
        ): ModuleDescriptor {
            val methods = methodsIn(elements.getAllMembers(moduleDefinitionType))
            val moduleMirror = MoreElements.getAnnotationMirror(moduleDefinitionType, Module::class.java).get()
            val providesMethods = methods
                    .filter { isAnnotationPresent(it, Provides::class.java) }
                    .map { methodFactory.forProvidesMethod(it, moduleDefinitionType) }
            return ModuleDescriptor(
                moduleDefinitionType,
                moduleMirror,
                providesMethods.toImmutableList()
            )
        }
    }

    data
    class MethodDescriptor(
        val element: ExecutableElement,
        val binding: Binding
    ) {
        class Factory
        @Inject constructor(
            val elements: Elements,
            val types: SourcererTypes,
            val bindingFactory: Binding.Factory
        ) {
            fun forProvidesMethod(
                methodElement: ExecutableElement,
                contibutingModule: TypeElement
            ) = MethodDescriptor(
                methodElement,
                bindingFactory.forProvisionMethod(methodElement, contibutingModule)
            )
        }
    }
}
