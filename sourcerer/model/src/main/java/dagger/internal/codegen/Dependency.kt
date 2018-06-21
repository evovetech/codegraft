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

package dagger.internal.codegen

import com.google.common.base.Preconditions.checkState
import com.google.common.collect.ImmutableSet
import com.google.common.collect.Iterables.getOnlyElement
import dagger.internal.codegen.RequestKinds.extractKeyType
import dagger.internal.codegen.RequestKinds.getRequestKind
import dagger.model.RequestKind
import dagger.model.Scope
import javax.inject.Inject
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.ExecutableType
import javax.lang.model.type.TypeMirror

data
class Dependency(
    override val key: Key,
    val kind: RequestKind,
    val requestElement: Element? = null,
    val scope: Scope? = null
) : Keyed {

    internal
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
            return newDependencyRequest(
                requestElement = variableElement,
                type = resolvedType,
                qualifier = qualifier,
                scope = variableElement.uniqueScope
            )
        }

        fun forComponentProvisionMethod(
            provisionMethod: ExecutableElement,
            provisionMethodType: ExecutableType
        ): Dependency = newDependencyRequest(
            requestElement = provisionMethod,
            type = provisionMethodType.returnType,
            qualifier = provisionMethod.qualifier
        )

        fun forComponentMembersInjectionMethod(
            membersInjectionMethod: ExecutableElement,
            membersInjectionMethodType: ExecutableType
        ): Dependency {
            val membersInjectedType = getOnlyElement<TypeMirror>(membersInjectionMethodType.parameterTypes)
            return Dependency(
                key = keyFactory.forMembersInjectedType(membersInjectedType),
                kind = RequestKind.MEMBERS_INJECTION,
                requestElement = membersInjectionMethod
            )
        }

        private fun newDependencyRequest(
            type: TypeMirror,
            requestElement: Element? = null,
            qualifier: AnnotationMirror? = null,
            scope: Scope? = null
        ): Dependency {
            val kind = getRequestKind(type)
            val keyType = extractKeyType(kind, type)
            return Dependency(
                key = keyFactory.forQualifiedType(keyType, qualifier),
                kind = kind,
                requestElement = requestElement,
                scope = scope
            )
        }
    }
}
