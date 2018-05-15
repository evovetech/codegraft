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

package sourcerer.model

import com.google.auto.value.AutoValue
import dagger.model.Key
import dagger.model.RequestKind
import javax.lang.model.element.Element

//class

@AutoValue
abstract
class DependencyRequest : dagger.model.DependencyRequest() {

    @AutoValue.Builder
    abstract
    class Builder : dagger.model.DependencyRequest.Builder() {
        abstract override
        fun requestElement(element: Element): Builder

        abstract override
        fun key(key: Key): Builder

        abstract override
        fun kind(kind: RequestKind): Builder

        abstract override
        fun isNullable(isNullable: Boolean): Builder

        abstract override
        fun build(): DependencyRequest
    }
}

fun dependencyRequest(
    init: DependencyRequest.Builder.() -> Unit
): DependencyRequest {
    val builder = AutoValue_DependencyRequest.Builder()
            .isNullable(false)
    builder.init()
    return builder.build()
}
