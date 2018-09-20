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

import com.android.build.api.transform.Format.JAR
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.Status.ADDED
import java.util.jar.JarFile

class JarFileInput(
    root: JarInput
) : Input<JarInput>(root) {
    override
    val format = JAR

    override
    fun entries(incremental: Boolean): List<Entry> {
        val parent = file.toRelPath()
        val status = if (incremental) {
            root.status
        } else {
            ADDED
        }

        val jar = JarFile(file)
        return jar.entries().toList().map { entry ->
            JarPathEntry(parent, jar, entry, status)
        }
    }
}
