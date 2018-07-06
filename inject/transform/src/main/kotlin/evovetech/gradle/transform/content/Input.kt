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
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import java.io.File

class DirInput : Input<DirectoryInput> {
    constructor(
        root: DirectoryInput
    ) : super(root)

    constructor(
        root: DirectoryInput,
        base: File
    ) : super(root, base)

    constructor(
        root: DirectoryInput,
        base: File,
        child: File
    ) : super(root, base, child)

    override
    val format = Format.DIRECTORY

    override
    fun create(base: File, rel: File): DirInput {
        return DirInput(root, base, rel)
    }
}

class JarFileInput : Input<JarInput> {
    constructor(
        root: JarInput
    ) : super(root)

    constructor(
        root: JarInput,
        base: File
    ) : super(root, base)

    constructor(
        root: JarInput,
        base: File,
        child: File
    ) : super(root, base, child)

    override
    val format = Format.JAR

    override
    fun create(base: File, rel: File): JarFileInput {
        return JarFileInput(root, base, rel)
    }
}
