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

package sourcerer

import com.google.common.collect.ImmutableSetMultimap
import com.google.common.collect.SetMultimap
import javax.lang.model.element.Element

typealias AnnotationElements = SetMultimap<Class<out Annotation>, Element>
typealias ImmutableAnnotationElements = ImmutableSetMultimap<Class<out Annotation>, Element>

fun <A : Annotation> AnnotationElements.inputs(type: Class<A>) = get(type).map {
    val anno: A = it.getAnnotation(type)
    Input(anno, it)
}

inline
fun <reified A : Annotation> AnnotationElements.inputs() =
    inputs(A::class.java)

inline
fun <reified A : Annotation> AnnotationElements.typeInputs() = inputs<A>()
        .mapNotNull(AnnotatedElement<A, *>::asTypeElement)
