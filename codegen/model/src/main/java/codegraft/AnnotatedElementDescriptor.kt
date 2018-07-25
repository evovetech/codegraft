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

package codegraft

import codegraft.bootstrap.className
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
