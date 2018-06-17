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

package sourcerer.inject.model

import com.google.auto.common.MoreTypes.asPrimitiveType
import com.google.common.collect.ImmutableSet
import javax.inject.Inject
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Types

class SourcererTypes
@Inject constructor(
    private val delegate: Types
) : Types by delegate {

    fun boxedType(
        type: TypeMirror
    ): TypeMirror = if (type.kind.isPrimitive) {
        boxedClass(asPrimitiveType(type)).asType()
    } else {
        type
    }
}

inline
fun <reified E> immutableSet(init: ImmutableSet.Builder<E>.() -> Unit): ImmutableSet<E> {
    val builder = ImmutableSet.builder<E>()
    builder.init()
    return builder.build()
}
