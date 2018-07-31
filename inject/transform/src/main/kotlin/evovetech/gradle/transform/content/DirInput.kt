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

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format.DIRECTORY

class DirInput(
    root: DirectoryInput
) : Input<DirectoryInput>(root) {

    override
    val format = DIRECTORY

    override
    fun entries(incremental: Boolean): List<Entry> {
        val parent = file.toRelPath()
        if (!incremental) {
            return entries(parent)
        }

        if (!file.exists()) {
            return emptyList()
        }

        return root.changedFiles.map { (file, status) ->
            val path = parent.withChild(file)
            RelPathEntry(path, status)
        }
    }
}
