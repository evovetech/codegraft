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

import com.android.build.api.transform.Status
import java.io.FileInputStream
import java.io.InputStream

class RelPathEntry(
    override
    val relPath: RelPath,

    override
    val status: Status
) : Entry {

    constructor(
        relPath: RelPath
    ) : this(relPath, Status.ADDED)

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
