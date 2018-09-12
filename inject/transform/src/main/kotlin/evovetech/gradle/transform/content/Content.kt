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

import com.android.build.api.transform.Format
import com.android.build.api.transform.Format.DIRECTORY
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Status.ADDED
import com.android.build.api.transform.Status.CHANGED
import com.android.build.api.transform.Status.NOTCHANGED
import com.android.build.api.transform.Status.REMOVED
import com.android.build.api.transform.TransformInvocation
import com.android.utils.FileUtils
import net.bytebuddy.dynamic.ClassFileLocator
import net.bytebuddy.dynamic.ClassFileLocator.ForFolder
import net.bytebuddy.dynamic.ClassFileLocator.ForJarFile
import java.io.File
import java.util.jar.JarFile

sealed
class Content(
    val file: File
)

abstract
class Input<out T : QualifiedContent>(
    val root: T
) : Content(root.file) {
    abstract
    val format: Format

    abstract
    fun entries(incremental: Boolean): List<Entry>

    val classFileLocator: ClassFileLocator by lazy {
        format.let {
            if (it == DIRECTORY) {
                ForFolder(file)
            } else {
                val jarFile = JarFile(file)
                ForJarFile(jarFile)
            }
        }
    }
}

class ParentOutput(
    val input: Input<*>,
    file: File
) : Content(file) {

    fun outputs(incremental: Boolean): List<Output> {
        if (!incremental) {
            FileUtils.deleteRecursivelyIfExists(file)
            return input.entries(incremental)
                    .filterNot(Entry::isDirectory)
                    .map { Output(it, file) }
        }

        val rootStatus = when (input) {
            is DirInput -> {
                if (input.file.exists()) {
                    ADDED
                } else {
                    REMOVED
                }
            }
            is JarInput -> input.status
            else -> return emptyList()
        }

        when (rootStatus) {
            REMOVED -> {
                FileUtils.deleteRecursivelyIfExists(file)
                return emptyList()
            }
            NOTCHANGED -> {
                return emptyList()
            }
            CHANGED -> {
                FileUtils.deleteRecursivelyIfExists(file)
                // continue
            }
            else -> {
                // continue
            }
        }

        return input.entries(incremental)
                .filter { entry ->
                    val status = entry.status
                    when (status) {
                        REMOVED -> {
                            FileUtils.deleteRecursivelyIfExists(entry.relPath.file)
                            false
                        }

                        NOTCHANGED -> false

                        ADDED,
                        CHANGED -> true
                    }
                }
                .filterNot(Entry::isDirectory)
                .map { Output(it, file) }
    }

    companion object {
        @JvmStatic
        fun root(
            invocation: TransformInvocation,
            input: Input<*>
        ): ParentOutput = ParentOutput(input, invocation.run {
            val root = input.root
            outputProvider.getContentLocation(root.name, root.contentTypes, root.scopes, Format.DIRECTORY)
        })
    }
}

class Output(
    val input: Entry,
    val root: File,
    path: RelPath = RelPath.Factory.create(root, input.relPath.rel)
) : Content(path.file),
    RelPath by path {

    fun copyToDest(): Long = try {
        file.parentFile?.mkdirs()
        input.copyTo(file)
    } catch (e: Throwable) {
        // TODO:
        e.printStackTrace(); 0
    }
}
