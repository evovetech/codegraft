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

package evovetech.blog.medium

import org.mockito.Mockito
import java.io.File

val ModulePath: String by lazy {
    ":blog:medium".split(":")
            .filter(String::isNotEmpty)
            .joinToString(prefix = "/")
}
val Module: File by lazy(ModulePath::moduleDir)

inline
fun <reified T : Any> mock(
    noinline init: (T) -> Unit = {}
) = lazy {
    Mockito.mock(T::class.java)
            .apply(init)
}

val String.moduleDir: File
    get() {
        val relDir = File(this)
        val curDir = File(".")
                .absoluteFile
                .parentFile
        return if (relDir.name.equals(curDir.name, ignoreCase = true)) {
            curDir
        } else {
            File(curDir, relDir.path)
        }
    }