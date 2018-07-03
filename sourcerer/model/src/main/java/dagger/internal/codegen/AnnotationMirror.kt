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

import com.google.auto.common.AnnotationMirrors
import com.google.auto.common.AnnotationMirrors.getAnnotationValue
import com.google.common.base.Equivalence
import com.google.common.collect.ImmutableList
import dagger.internal.codegen.MoreAnnotationValues.asAnnotationValues
import dagger.internal.codegen.MoreAnnotationValues.asType
import sourcerer.bootstrap.toImmutableList
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Name
import javax.lang.model.type.TypeMirror

val AnnotationMirror.simpleName: Name
    get() = annotationType.asElement().simpleName

fun AnnotationMirror?.wrap(): Equivalence.Wrapper<AnnotationMirror>? =
    this?.let { AnnotationMirrors.equivalence().wrap(it) }

/**
 * Returns the value named `name` from `annotation`.
 *
 * @throws IllegalArgumentException unless that member represents a single type
 */
fun AnnotationMirror.getTypeValue(
    name: String
): TypeMirror {
    return asType(getAnnotationValue(this, name))
}

fun AnnotationMirror.getTypeListValue(
    name: String
): ImmutableList<TypeMirror> {
    return asAnnotationValues(getAnnotationValue(this, name))
            .map(MoreAnnotationValues::asType)
            .toImmutableList()
}
