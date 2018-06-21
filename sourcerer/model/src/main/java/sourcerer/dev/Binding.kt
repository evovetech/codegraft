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

import com.google.auto.common.MoreTypes
import com.google.auto.common.MoreTypes.asExecutable
import com.google.common.collect.ImmutableSet
import dagger.model.Scope
import javax.inject.Inject
import javax.lang.model.element.Element
import javax.lang.model.element.ElementVisitor
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.util.SimpleElementVisitor6

interface BindDec {
    val key: Key
    val dependencies: ImmutableSet<Dependency>
    val bindingElement: Element?
    val contributionModule: TypeElement?
    val bindingTypeElement: TypeElement?
        get() = bindingElement?.accept<TypeElement, Void>(ENCLOSING_TYPE_ELEMENT, null)
}

interface Keyed {
    val key: Key
}

/** An object that declares or specifies a binding.  */
data
class Binding(
    override val key: Key,
    val dependencies: ImmutableSet<Dependency> = ImmutableSet.of(),
    val bindingElement: Element? = null,
    val contributionModule: TypeElement? = null,
    val scope: Scope? = null
) : Keyed {
    val bindingTypeElement: TypeElement? by lazy {
        bindingElement?.accept<TypeElement, Void>(ENCLOSING_TYPE_ELEMENT, null)
    }

    class Factory
    @Inject constructor(
        val types: SourcererTypes,
        val elements: SourcererElements,
        val keyFactory: Key.Factory,
        val dependencyFactory: Dependency.Factory
    ) {
        fun forProvisionMethod(
            method: ExecutableElement,
            contributingModule: TypeElement
        ): Binding {
            val methodType = asExecutable(types.asMemberOf(MoreTypes.asDeclared(contributingModule.asType()), method))
            val returnType = methodType.returnType
            return Binding(
                key = keyFactory.forMethod(method, returnType),
                dependencies = dependencyFactory.forRequiredResolvedVariables(
                    method.parameters,
                    methodType.parameterTypes
                ),
                bindingElement = method,
                contributionModule = contributingModule,
                scope = method.uniqueScope
            )
        }
    }
}

/**
 * A visitor that returns the input or the closest enclosing element that is a
 * [TypeElement].
 */
val ENCLOSING_TYPE_ELEMENT: ElementVisitor<TypeElement, Void> = object : SimpleElementVisitor6<TypeElement, Void>() {
    override fun defaultAction(e: Element, p: Void?): TypeElement {
        return visit(e.enclosingElement)
    }

    override
    fun visitType(e: TypeElement, p: Void): TypeElement {
        return e
    }
}
