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

import com.google.common.collect.ImmutableList
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.type.TypeMirror

/**
 * Returns the value named `name` from `annotation`.
 *
 * @throws IllegalArgumentException unless that member represents a single type
 */
fun AnnotationMirror.getTypeValue(name: String): TypeMirror =
    MoreAnnotationMirrors.getTypeValue(this, name)

fun AnnotationMirror.getTypeListValue(name: String): ImmutableList<TypeMirror> =
    MoreAnnotationMirrors.getTypeListValue(this, name)
