/*
 * Copyright (C) 2018 evove.tech
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dagger.internal.codegen

import codegraft.inject.BootScope
import com.google.common.collect.ImmutableSet
import com.google.common.collect.Iterables.getOnlyElement
import dagger.model.Scope
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements

/**
 * Creates a [Scope] object from the [javax.inject.Scope]-annotated annotation type.
 */
val TypeElement.scope: Scope
    get() = Scope.scope(SimpleAnnotationMirror.of(this))

/** Returns a representation for [@BootScope][BootScope] scope.  */
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
val Scope.readableSource: String
    get() = Scopes.getReadableSource(this)

/** Returns all of the associated scopes for a source code element.  */
val Element.scopes: ImmutableSet<Scope>
    get() = Scopes.scopesOf(this)

private fun Elements.scope(
    scopeAnnotationClass: Class<out Annotation>
): Scope {
    return getTypeElement(scopeAnnotationClass.canonicalName).scope
}
