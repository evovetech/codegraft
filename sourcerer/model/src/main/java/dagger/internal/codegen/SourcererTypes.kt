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

import com.google.auto.common.MoreTypes.asPrimitiveType
import java.util.Optional
import javax.inject.Inject
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Types

internal class SourcererTypes
@Inject constructor(
    val elements: SourcererElements,
    val types: DaggerTypes
) : Types by types {

    fun boxedType(
        type: TypeMirror
    ): TypeMirror = if (type.kind.isPrimitive) {
        boxedClass(asPrimitiveType(type)).asType()
    } else {
        type
    }

    fun nonObjectSuperclass(type: DeclaredType): Optional<DeclaredType> {
        return types.nonObjectSuperclass(type)
    }

    fun unwrapTypeOrObject(type: TypeMirror): TypeMirror {
        return types.unwrapTypeOrObject(type)
    }

    fun rewrapType(type: TypeMirror, wrappingClass: Class<*>): DeclaredType {
        return types.rewrapType(type, wrappingClass)
    }

    inline
    fun <reified T> rewrapType(
        type: TypeMirror
    ): TypeMirror = rewrapType(type, T::class.java)

    fun wrapType(type: TypeMirror, wrappingClass: Class<*>): TypeMirror {
        return types.wrapType(type, wrappingClass)
    }

    inline
    fun <reified T> wrapType(
        type: TypeMirror
    ): TypeMirror = wrapType(type, T::class.java)

    fun unwrapType(type: TypeMirror): TypeMirror {
        return types.unwrapType(type)
    }
}
