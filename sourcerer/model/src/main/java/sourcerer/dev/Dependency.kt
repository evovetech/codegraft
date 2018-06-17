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

import com.google.common.base.Preconditions.checkState
import com.google.common.collect.ImmutableSet
import javax.inject.Inject
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror

data
class Dependency(
    val key: Key,
    val requestElement: Element? = null
) {

    class Factory
    @Inject constructor(
        val types: SourcererTypes,
        val keyFactory: Key.Factory
    ) {
        fun forRequiredResolvedVariables(
            variables: List<VariableElement>,
            resolvedTypes: List<TypeMirror>
        ): ImmutableSet<Dependency> {
            checkState(resolvedTypes.size == variables.size)
            return immutableSet {
                for (i in variables.indices) {
                    add(forRequiredResolvedVariable(variables[i], resolvedTypes[i]))
                }
            }
        }

        fun forRequiredResolvedVariable(
            variableElement: VariableElement,
            resolvedType: TypeMirror
        ): Dependency {
            val qualifier = variableElement.qualifier
            return newDependencyRequest(variableElement, resolvedType, qualifier)
        }

        private fun newDependencyRequest(
            requestElement: Element,
            type: TypeMirror,
            qualifier: AnnotationMirror? = null
        ) = Dependency(
            key = keyFactory.forQualifiedType(type, qualifier),
            requestElement = requestElement
        )
    }
}
