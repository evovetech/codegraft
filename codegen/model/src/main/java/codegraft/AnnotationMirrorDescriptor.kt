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

package codegraft

import com.google.auto.common.AnnotationMirrors
import com.google.common.base.Equivalence
import javax.lang.model.element.AnnotationMirror

abstract
class AnnotationDescriptor {
    abstract
    val mirror: AnnotationMirror

    val wrapper: Equivalence.Wrapper<AnnotationMirror> by lazy {
        AnnotationMirrors.equivalence().wrap(mirror)
    }

    final override
    fun equals(other: Any?): Boolean = when {
        this === other -> true
        other is AnnotationDescriptor -> {
            wrapper == other.wrapper
        }
        else -> false
    }

    final override
    fun hashCode(): Int =
        wrapper.hashCode()
}

fun AnnotationMirror.toDescriptor() = object : AnnotationDescriptor() {
    override
    val mirror: AnnotationMirror = this@toDescriptor
}
