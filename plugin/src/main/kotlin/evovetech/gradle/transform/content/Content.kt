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
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.TransformInvocation
import com.android.utils.FileUtils
import evovetech.plugin.util.Counter
import net.bytebuddy.dynamic.ClassFileLocator
import net.bytebuddy.dynamic.ClassFileLocator.ForFolder
import net.bytebuddy.dynamic.ClassFileLocator.ForJarFile
import java.io.File
import java.util.jar.JarFile

sealed
class Content : RelPath {
    val parent: File
        get() = base
}

abstract
class BaseContent<out T : Content>(
    override val base: File,
    rel: File? = null
) : Content(),
    RelPath.Typed<T> {

    override
    val rel: File = rel ?: File("")

    open
    fun listFiles() = file.run {
        if (!isDirectory) {
            println("returning empty for $this")
            return emptyList<T>()
        }
        listFiles()
    }.map(this::withChild)
}

fun <T : BaseContent<T>> flatMapDirs(input: T): Iterable<T> {
    if (!input.isDirectory) {
        return listOf(input)
    }
    return input.listFiles()
            .flatMap(::flatMapDirs)
}

fun <T : BaseContent<T>> T.allFiles() = listFiles()
        .flatMap(::flatMapDirs)

abstract
class Input<out T : QualifiedContent>(
    val root: T,
    base: File = root.file,
    child: File? = null
) : BaseContent<Input<T>>(base, child) {
    abstract
    val format: Format

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

class Output(
    val input: Input<*>,
    base: File,
    child: File? = null
) : BaseContent<Output>(base, child) {
    init {
        if (input.isDirectory) {
            file.mkdirs()
        }
    }

    override
    fun create(base: File, rel: File): Output {
        return Output(input, base, rel)
    }

    override
    fun listFiles(): List<Output> = input.listFiles().map {
        Output(it, base, it.rel)
    }

    fun copyToDest() {
        if (!isDirectory) {
            val src = input.file
            val dest = file
            FileUtils.copyFile(src, dest)
        }
    }

    companion object {
        @JvmStatic
        fun root(
            invocation: TransformInvocation,
            input: Input<*>,
            format: Format = Format.DIRECTORY
        ): Output = invocation.run {
            val name = Counter.next
            val root = input.root
            outputProvider.getContentLocation(name, root.contentTypes, root.scopes, format)
        }.let {
            Output(input, it)
        }
    }
}
