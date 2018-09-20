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

import com.google.auto.common.MoreElements
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import kotlin.reflect.KClass

typealias AnnotationType = KClass<out Annotation>

interface AnnotatedElement<out A : Annotation, out E : Element> {
    val annotation: A
    val element: E
}

operator
fun <A : Annotation> AnnotatedElement<A, *>.component1(): A = annotation

operator
fun <E : Element> AnnotatedElement<*, E>.component2(): E = element

data
class DefaultAnnotatedElement<out A : Annotation, out E : Element>(
    override val annotation: A,
    override val element: E
) : AnnotatedElement<A, E>

data
class AnnotatedTypeElement<out A : Annotation>(
    override val annotation: A,
    override val element: TypeElement
) : AnnotatedElement<A, TypeElement>

fun <A : Annotation> AnnotatedElement<A, *>.asTypeElement() = if (MoreElements.isType(element)) {
    AnnotatedTypeElement(annotation, MoreElements.asType(element))
} else {
    null
}

inline
fun <reified A : Annotation> Element.getAnnotation(): A? =
    getAnnotation(A::class.java)

inline
fun <reified A : Annotation, E : Element> E.annotatedWith(): AnnotatedElement<A, E>? =
    getAnnotation<A>()?.let { DefaultAnnotatedElement(it, this) }

inline
fun <reified A : Annotation, E : Element> Iterable<E>.annotatedWith() =
    mapNotNull { it.annotatedWith<A, E>() }

inline
fun <reified A : Annotation> TypeElement.annotatedWith(): AnnotatedTypeElement<A>? =
    annotatedWith<A, TypeElement>()
            ?.asTypeElement()

inline
fun <reified A : Annotation> Iterable<TypeElement>.typesAnnotatedWith() =
    annotatedWith<A, TypeElement>()

inline
fun <reified A : Annotation> RoundEnvironment.annotatedWith() =
    getElementsAnnotatedWith(A::class.java)
            .annotatedWith<A, Element>()

inline fun <reified A : Annotation> RoundEnvironment.elementsAnnotatedWith(): Set<Element> =
    getElementsAnnotatedWith(A::class.java)

inline fun <reified A : Annotation> RoundEnvironment.typesAnnotatedWith() =
    annotatedWith<A>().mapNotNull { (a, e) ->
        (e as? TypeElement)
                ?.let { AnnotatedTypeElement(a, it) }
    }
