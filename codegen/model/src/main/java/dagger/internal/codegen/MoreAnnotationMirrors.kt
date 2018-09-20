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
