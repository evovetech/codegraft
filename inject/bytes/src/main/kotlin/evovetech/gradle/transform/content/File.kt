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

import net.bytebuddy.dynamic.ClassFileLocator
import java.io.File
import java.util.jar.JarFile

val File.dirs
    get() = if (isDirectory) {
        emptyList()
    } else {
        listFiles().filter { it.isDirectory }
    }
val File.files
    get() = if (isDirectory) {
        listFiles().filterNot { it.isDirectory }
    } else {
        listOf(this)
    }

val File.classFileLocator: ClassFileLocator
    get() = if (isDirectory) {
        ClassFileLocator.ForFolder(this)
    } else {
        val jarFile = JarFile(this)
        ClassFileLocator.ForJarFile(jarFile)
    }

