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
