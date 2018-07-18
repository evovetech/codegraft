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

import java.io.FileInputStream
import java.io.InputStream

class RelPathEntry(
    override
    val relPath: RelPath
) : Entry {

    override
    val path: String
        get() = relPath.rel.path

    override
    val isDirectory: Boolean
        get() = relPath.isDirectory

    override
    fun newInputStream(): InputStream {
        return FileInputStream(relPath.file)
    }
}

fun entries(parent: RelPath): List<Entry> {
    val entries = ArrayList<Entry>()
    val children = parent.children
    val dirs = children.filter(RelPath::isDirectory)
    entries.addAll(children.map(::RelPathEntry))
    entries.addAll(dirs.flatMap(::entries))
    return entries
}
