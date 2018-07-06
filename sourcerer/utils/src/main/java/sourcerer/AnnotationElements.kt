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
