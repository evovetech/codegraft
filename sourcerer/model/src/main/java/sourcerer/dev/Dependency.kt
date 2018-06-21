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
import dagger.Lazy
import dagger.model.Scope
import dagger.producers.Produced
import dagger.producers.Producer
import javax.inject.Inject
import javax.inject.Provider
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.ExecutableType
import javax.lang.model.type.TypeMirror

data
class Dependency(
    override val key: Key,
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
                variableElement,
                resolvedType,
                qualifier,
                variableElement.uniqueScope
            )
        }

        fun forComponentProvisionMethod(
            provisionMethod: ExecutableElement,
            provisionMethodType: ExecutableType
        ): Dependency = newDependencyRequest(
            provisionMethod,
            provisionMethodType.returnType,
            provisionMethod.qualifier
        )

        private fun newDependencyRequest(
            requestElement: Element,
            type: TypeMirror,
            qualifier: AnnotationMirror? = null,
            scope: Scope? = null
        ) = Dependency(
            key = keyFactory.forQualifiedType(type, qualifier),
            requestElement = requestElement,
            scope = scope
        )
    }
}

/**
 * Represents the different kinds of [types][javax.lang.model.type.TypeMirror] that may be
 * requested as dependencies for the same key. For example, `String`, `Provider<String>`, and `Lazy<String>` can all be requested if a key exists for `String`; they have the [.INSTANCE], [.PROVIDER], and [.LAZY] request kinds,
 * respectively.
 */
enum class RequestKind {
    /** A default request for an instance. E.g.: `FooType`  */
    INSTANCE,

    /** A request for a [Provider]. E.g.: `Provider<FooType>`  */
    PROVIDER,

    /** A request for a [Lazy]. E.g.: `Lazy<FooType>`  */
    LAZY,

    /** A request for a [Provider] of a [Lazy]. E.g.: `Provider<Lazy<FooType>>`  */
    PROVIDER_OF_LAZY,

    /**
     * A request for a members injection. E.g. `void inject(FooType);`. Can only be requested by
     * component interfaces.
     */
    MEMBERS_INJECTION,

    /** A request for a [Producer]. E.g.: `Producer<FooType>`  */
    PRODUCER,

    /** A request for a [Produced]. E.g.: `Produced<FooType>`  */
    PRODUCED,

    /**
     * A request for a [com.google.common.util.concurrent.ListenableFuture]. E.g.: `ListenableFuture<FooType>`. These can only be requested by component interfaces.
     */
    FUTURE
}
