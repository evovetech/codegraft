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
import evovetech.gradle.transform.content.RelPath.Factory
import java.io.File
import java.io.InputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile

class JarPathEntry(
    private val root: RelPath,
    private val jar: JarFile,
    private val actual: JarEntry,

    override
    val status: Status
) : Entry {

    override
    val path: String
        get() = actual.name

    override
    val relPath: RelPath
        get() = Factory.create(root.base, File(path))

    override
    val isDirectory: Boolean
        get() = actual.isDirectory

    override
    fun newInputStream(): InputStream {
        return jar.getInputStream(actual)
    }
}
