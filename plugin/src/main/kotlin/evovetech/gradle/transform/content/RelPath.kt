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

package evovetech.gradle.transform.content

import evovetech.gradle.transform.content.RelPath.Typed
import java.io.File

interface RelPath {
    val base: File
    val rel: File
    val file: File
        get() = File(base, rel.path)
    val isRoot: Boolean
        get() = base == file
    val isDirectory: Boolean
        get() = file.isDirectory

    fun create(base: File, rel: File): RelPath

    interface Typed<out T : RelPath> : RelPath {
        override
        fun create(base: File, rel: File): T
    }
}

fun <T : RelPath> Typed<T>.ensure(): T = create(
    base,
    rel.relativeTo(base)
)

fun <T : RelPath> Typed<T>.withChild(file: File): T = create(
    base,
    file.relativeTo(base)
)
