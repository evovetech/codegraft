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

package sourcerer

import com.google.auto.common.MoreElements
import com.google.auto.common.MoreTypes
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import kotlin.reflect.KClass

typealias AnnotationType = KClass<out Annotation>

interface AnnotatedElement<out A : Annotation, out E : Element> {
    val annotation: A
    val element: E
}

fun say() {
//    MoreTypes.equivalence().wrap()
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
