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

import java.io.File

interface RelPath {
    val base: File
    val rel: File

    object Factory {
        @JvmStatic
        fun create(base: File, rel: File? = null): RelPath {
            return DefaultRelPath(base, rel ?: File(""))
        }

        private data
        class DefaultRelPath(
            override val base: File,
            override val rel: File
        ) : RelPath
    }
}

fun File.toRelPath(): RelPath {
    return RelPath.Factory.create(this)
}

val RelPath.file: File
    get() = File(base, rel.path)

val RelPath.isRoot: Boolean
    get() = base == file

val RelPath.isDirectory: Boolean
    get() = file.isDirectory

val RelPath.children: List<RelPath>
    get() = file.listFiles()
            .map(::withChild)

fun RelPath.ensure(): RelPath = RelPath.Factory.create(
    base,
    rel.relativeTo(base)
)

fun RelPath.withChild(file: File): RelPath = RelPath.Factory.create(
    base,
    file.relativeTo(base)
)
