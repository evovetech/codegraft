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

import com.google.auto.common.MoreElements.isAnnotationPresent
import com.google.common.collect.ImmutableList
import dagger.Provides
import javax.inject.Inject
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.util.ElementFilter.methodsIn
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

data
class ModuleDescriptor(
    val definitionType: TypeElement,
    val providesMethods: ImmutableList<ExecutableElement>
) {
    class Factory
    @Inject constructor(
        val elements: Elements,
        val types: Types
    ) {
        fun create(
            moduleElement: TypeElement
        ): ModuleDescriptor {
            val methods = methodsIn(elements.getAllMembers(moduleElement))
            val providesMethods = methods.filter { moduleMethod ->
                isAnnotationPresent(moduleMethod, Provides::class.java)
            }
            return ModuleDescriptor(moduleElement, ImmutableList.copyOf(providesMethods))
        }
    }
}
