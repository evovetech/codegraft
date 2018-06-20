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

package sourcerer.codegen

import com.squareup.javapoet.MethodSpec

data
class MethodBuilder(
    val name: String,
    val init: MethodSpec.Builder.() -> Unit
) {
    fun withName(name: String): MethodBuilder {
        return copy(name = name)
    }

    fun build(): MethodSpec {
        val builder = MethodSpec.methodBuilder(name)
        builder.init()
        return builder.build()
    }
}

fun Collection<MethodBuilder>.buildUnique() = groupBy {
    it.name
}.flatMap { (key, methods) ->
    val size = methods.size
    val builders = if (size > 1) {
        var i = 1
        methods.map {
            val n = "$key${i++}"
            it.withName(n)
        }
    } else {
        methods
    }
    builders.map { it.build() }
}
