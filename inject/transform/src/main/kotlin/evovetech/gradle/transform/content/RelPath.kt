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
