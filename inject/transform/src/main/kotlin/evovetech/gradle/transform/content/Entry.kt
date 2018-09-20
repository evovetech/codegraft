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
import java.io.File
import java.io.InputStream
import java.io.OutputStream

interface Entry {
    val path: String
    val relPath: RelPath
    val isDirectory: Boolean
    val status: Status
    fun newInputStream(): InputStream
}

fun InputStream.copyTo(
    dest: OutputStream
): Long = copyTo(out = dest)

fun InputStream.copyTo(
    file: File
): Long = file.outputStream()
        .use(this::copyTo)

fun File.copyFrom(
    src: InputStream
): Long = src.copyTo(this)

fun Entry.copyTo(
    file: File
): Long = newInputStream()
        .use(file::copyFrom)
