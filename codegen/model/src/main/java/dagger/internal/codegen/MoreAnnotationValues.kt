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
import javax.lang.model.element.AnnotationValue
import javax.lang.model.type.TypeMirror

fun AnnotationValue.asType(): TypeMirror =
    MoreAnnotationValues.asType(this)

fun AnnotationValue.asAnnotationValues(): ImmutableList<AnnotationValue> =
    MoreAnnotationValues.asAnnotationValues(this)
