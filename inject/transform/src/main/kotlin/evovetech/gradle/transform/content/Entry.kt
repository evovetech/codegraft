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
