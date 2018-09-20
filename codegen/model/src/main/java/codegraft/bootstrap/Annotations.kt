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

package codegraft.bootstrap

import com.google.auto.common.AnnotationMirrors.getAnnotatedAnnotations
import com.google.auto.common.MoreElements.isAnnotationPresent
import com.google.common.collect.ImmutableSet
import sourcerer.toImmutableSet
import javax.inject.Inject
import javax.inject.Qualifier
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.util.ElementFilter.constructorsIn

const val BOOTSTRAP_DEPENDENCIES_ATTRIBUTE = "bootstrapDependencies"
const val BOOTSTRAP_MODULES_ATTRIBUTE = "bootstrapModules"
const val APPLICATION_MODULES_ATTRIBUTE = "applicationModules"
const val AUTO_INCLUDE_ATTRIBUTE = "autoInclude"
const val FLATTEN_ATTRIBUTE = "flatten"
const val DEPENDENCIES_ATTRIBUTE = "dependencies"
const val MODULES_ATTRIBUTE = "modules"

val Element.qualifier: AnnotationMirror?
    get() {
        val qualifierAnnotations = qualifiers
        return when (qualifierAnnotations.size) {
            0 -> null
            1 -> qualifierAnnotations.first()
            else -> throw IllegalArgumentException(
                toString() + " was annotated with more than one @Qualifier annotation"
            )
        }
    }

val Element.qualifiers: ImmutableSet<out AnnotationMirror>
    get() = getAnnotatedAnnotations(this, Qualifier::class.java)

/** Returns the constructors in `type` that are annotated with [Inject].  */
val TypeElement.injectedConstructors: ImmutableSet<ExecutableElement>
    get() = constructorsIn(enclosedElements)
            .filter { constructor -> isAnnotationPresent(constructor, Inject::class.java) }
            .toImmutableSet()
