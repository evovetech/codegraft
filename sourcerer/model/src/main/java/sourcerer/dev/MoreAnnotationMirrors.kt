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
import com.google.auto.common.AnnotationMirrors.getAnnotationValue
import com.google.common.base.Equivalence
import com.google.common.collect.ImmutableList
import sourcerer.dev.MoreAnnotationValues.asAnnotationValues
import java.util.Optional
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Name
import javax.lang.model.type.TypeMirror

/**
 * A utility class for working with [AnnotationMirror] instances, similar to [ ].
 */
internal object MoreAnnotationMirrors {

    /**
     * Wraps an [Optional] of a type in an `Optional` of a [Equivalence.Wrapper] for
     * that type.
     */
    fun wrapOptionalInEquivalence(
        optional: Optional<AnnotationMirror>
    ): Optional<Equivalence.Wrapper<AnnotationMirror>> {
        return optional.map({
            AnnotationMirrors.equivalence()
                    .wrap(it)
        })
    }

    /**
     * Unwraps an [Optional] of a [Equivalence.Wrapper] into an `Optional` of the
     * underlying type.
     */
    fun unwrapOptionalEquivalence(
        wrappedOptional: Optional<Equivalence.Wrapper<AnnotationMirror>>
    ): Optional<AnnotationMirror> {
        return wrappedOptional.map({ it.get() })
    }

    fun simpleName(annotationMirror: AnnotationMirror): Name {
        return annotationMirror.annotationType.asElement().simpleName
    }

    /**
     * Returns the value named `name` from `annotation`.
     *
     * @throws IllegalArgumentException unless that member represents a single type
     */
    fun getTypeValue(annotation: AnnotationMirror, name: String): TypeMirror {
        return MoreAnnotationValues.asType(getAnnotationValue(annotation, name))
    }

    /**
     * Returns the list of types that is the value named `name` from `annotationMirror`.
     *
     * @throws IllegalArgumentException unless that member represents an array of types
     */
    fun getTypeListValue(
        annotationMirror: AnnotationMirror, name: String
    ): ImmutableList<TypeMirror> {
        return asAnnotationValues(getAnnotationValue(annotationMirror, name))
                .map({ MoreAnnotationValues.asType(it) })
                .let { ImmutableList.copyOf(it) }
    }
}
