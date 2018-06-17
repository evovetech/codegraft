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

import com.google.auto.common.AnnotationMirrors
import com.google.common.collect.ImmutableSet
import com.google.common.collect.Iterables.getOnlyElement
import dagger.model.Scope
import sourcerer.inject.BootScope
import javax.inject.Singleton
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements

/**
 * Creates a [Scope] object from the [javax.inject.Scope]-annotated annotation type.
 */
val TypeElement.scope: Scope
    get() = Scope.scope(SimpleAnnotationMirror.of(this))

/** Returns a representation for [@Singleton][Singleton] scope.  */
val Elements.bootScope: Scope
    get() = scope(BootScope::class.java)

/**
 * Returns at most one associated scoped annotation from the source code element, throwing an
 * exception if there are more than one.
 */
val Element.uniqueScope: Scope?
    get() = getOnlyElement<Scope>(scopes, null)

/**
 * Returns the readable source representation (name with @ prefix) of the scope's annotation type.
 *
 *
 * It's readable source because it has had common package prefixes removed, e.g.
 * `@javax.inject.Singleton` is returned as `@Singleton`.
 *
 *
 * Does not return any annotation values, since [@Scope][javax.inject.Scope] annotations
 * are not supposed to have any.
 */
fun getReadableSource(scope: Scope): String {
    return stripCommonTypePrefixes("@" + scope.scopeAnnotationElement().qualifiedName)
}

/** Returns all of the associated scopes for a source code element.  */
val Element.scopes: ImmutableSet<Scope>
    get() = AnnotationMirrors.getAnnotatedAnnotations(this, javax.inject.Scope::class.java)
            .map { Scope.scope(it) }
            .toImmutableSet()

private fun Elements.scope(scopeAnnotationClass: Class<out Annotation>): Scope {
    return getTypeElement(scopeAnnotationClass.canonicalName).scope
}
