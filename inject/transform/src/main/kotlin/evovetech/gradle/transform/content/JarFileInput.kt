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
