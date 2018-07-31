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
