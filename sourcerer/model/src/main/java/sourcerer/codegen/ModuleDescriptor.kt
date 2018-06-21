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

import com.google.auto.common.MoreElements.getAnnotationMirror
import com.google.auto.common.MoreElements.isAnnotationPresent
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import dagger.Module
import dagger.Provides
import dagger.internal.codegen.Dependency
import javax.inject.Inject
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.TypeElement
import javax.lang.model.util.ElementFilter.methodsIn
import javax.lang.model.util.Elements

data
class ModuleDescriptor(
    val definitionType: TypeElement,
    val moduleMirror: AnnotationMirror,
    val provisionBindings: ImmutableList<Binding>
) {
    val dependencies: ImmutableSet<Dependency> by lazy {
        provisionBindings
                .flatMap(Binding::dependencies)
                .toImmutableSet()
    }

    internal
    class Factory
    @Inject constructor(
        val elements: Elements,
        val types: SourcererTypes,
        val bindingFactory: sourcerer.codegen.Binding.Factory
    ) {
        fun create(
            moduleDefinitionType: TypeElement
        ) = ModuleDescriptor(
            definitionType = moduleDefinitionType,
            moduleMirror = getAnnotationMirror(moduleDefinitionType, Module::class.java).get(),
            provisionBindings = methodsIn(elements.getAllMembers(moduleDefinitionType))
                    .filter { isAnnotationPresent(it, Provides::class.java) }
                    .map { bindingFactory.forProvisionMethod(it, moduleDefinitionType) }
                    .toImmutableList()
        )
    }
}
