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

import com.google.common.collect.ImmutableSet
import javax.inject.Inject
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import kotlin.reflect.KClass

internal class SourcererElements
@Inject constructor(
    val elements: DaggerElements,
    val types: DaggerTypes
) : Elements by elements {
    fun getUnimplementedMethods(type: TypeElement): ImmutableSet<ExecutableElement> =
        elements.getUnimplementedMethods(type)

    inline
    fun <reified T> getTypeElement(): TypeElement =
        getTypeElement(T::class)

    fun getTypeElement(klass: KClass<*>): TypeElement =
        elements.getTypeElement(klass.java)

    fun checkTypePresent(typeName: String): TypeElement =
        elements.checkTypePresent(typeName)

    companion object {
        /**
         * Returns `true` iff the given element has an [AnnotationMirror] whose
         * [annotation type][AnnotationMirror.getAnnotationType] has the same canonical name
         * as any of that of `annotationClasses`.
         */
        fun isAnyAnnotationPresent(
            element: Element,
            annotationClasses: Iterable<Class<out Annotation>>
        ): Boolean = DaggerElements.isAnyAnnotationPresent(element, annotationClasses)

        @SafeVarargs
        fun isAnyAnnotationPresent(
            element: Element,
            first: Class<out Annotation>,
            vararg otherAnnotations: Class<out Annotation>
        ): Boolean = DaggerElements.isAnyAnnotationPresent(
            element,
            first,
            *otherAnnotations
        )

        /**
         * Returns `true` iff the given element has an [AnnotationMirror] whose [ ][AnnotationMirror.getAnnotationType] is equivalent to `annotationType`.
         */
        fun isAnnotationPresent(
            element: Element,
            annotationType: TypeMirror
        ): Boolean = DaggerElements.isAnnotationPresent(
            element,
            annotationType
        )
    }
}

