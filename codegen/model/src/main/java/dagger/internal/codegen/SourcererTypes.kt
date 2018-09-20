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

import com.google.auto.common.MoreTypes.asPrimitiveType
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Types

internal
typealias SourcererTypes = DaggerTypes

internal
fun Types.boxedType(
    type: TypeMirror
): TypeMirror = if (type.kind.isPrimitive) {
    boxedClass(asPrimitiveType(type)).asType()
} else {
    type
}

internal inline
fun <reified T> SourcererTypes.rewrapType(
    type: TypeMirror
): TypeMirror = rewrapType(type, T::class.java)

internal inline
fun <reified T> SourcererTypes.wrapType(
    type: TypeMirror
): TypeMirror = wrapType(type, T::class.java)
