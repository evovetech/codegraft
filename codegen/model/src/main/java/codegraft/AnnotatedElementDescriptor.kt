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

import codegraft.plugins.className
import com.google.auto.common.MoreTypes
import com.google.common.base.Equivalence
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

abstract
class ElementDescriptor<E : Element> {
    abstract
    val element: E

    val type: TypeMirror by lazy {
        element.asType()
    }

    val typeWrapper: Equivalence.Wrapper<TypeMirror> by lazy {
        MoreTypes.equivalence().wrap(type)
    }

    final override
    fun equals(other: Any?): Boolean = when {
        this === other -> true
        other is AnnotatedElementDescriptor<*> -> {
            typeWrapper == other.typeWrapper
        }
        else -> false
    }

    final override
    fun hashCode(): Int =
        typeWrapper.hashCode()
}

abstract
class AnnotatedElementDescriptor<E : Element> : ElementDescriptor<E>() {
    abstract
    val annotation: AnnotationDescriptor
}

val AnnotatedElementDescriptor<TypeElement>.packageName: String
    get() = element.className.packageName()
