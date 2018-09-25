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

import com.android.build.api.transform.Format
import com.android.build.api.transform.Format.DIRECTORY
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Status
import com.android.build.api.transform.Status.ADDED
import com.android.build.api.transform.Status.CHANGED
import com.android.build.api.transform.Status.NOTCHANGED
import com.android.build.api.transform.Status.REMOVED
import com.android.build.api.transform.TransformInvocation
import com.android.utils.FileUtils
import evovetech.gradle.transform.OutputWriter
import evovetech.gradle.transform.TransformData
import net.bytebuddy.description.type.TypeDescription
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
    val entries: List<Entry>

    abstract
    fun changedFiles(
        incremental: Boolean
    ): List<Entry>

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
    entries: List<Entry>,
    file: File
) : Content(file) {

    val inputs = entries.filterNot { it.isDirectory }

    fun outputs(incremental: Boolean): List<Output> {
        if (!incremental) {
            FileUtils.deleteRecursivelyIfExists(file)
            return inputs.map { Output(it, file, Status.ADDED) }
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

        return when (rootStatus) {
            REMOVED -> {
                FileUtils.deleteRecursivelyIfExists(file)
                emptyList()
            }
            NOTCHANGED -> emptyList()
            else -> inputs.map { e ->
                Output(e, file, e.status)
            }
        }
    }

    companion object {
        @JvmStatic
        fun root(
            invocation: TransformInvocation,
            input: Input<*>,
            entries: List<Entry>
        ): ParentOutput = ParentOutput(input, entries, invocation.run {
            val root = input.root
            outputProvider.getContentLocation(root.name, root.contentTypes, root.scopes, Format.DIRECTORY)
        })
    }
}

class Output(
    val input: Entry,
    val root: File,
    val status: Status = input.status,
    val writer: OutputWriter? = null,
    path: RelPath = RelPath.Factory.create(root, input.relPath.rel)
) : Content(path.file),
    RelPath by path {

    fun perform(
        t: TransformData
    ): Map<TypeDescription?, File> = when (status) {
        REMOVED -> {
            FileUtils.delete(file)
            emptyMap()
        }
        NOTCHANGED -> emptyMap()
        ADDED,
        CHANGED -> writer?.let { w ->
            t.transform(w)
        } ?: t.copyToDest()
    }

    private
    fun TransformData.copyToDest(): Map<TypeDescription?, File> = file.let { f ->
        try {
            f.parentFile?.mkdirs()
            input.copyTo(f)
            mapOf(Pair(typeDescription, f))
        } catch (e: Throwable) {
            // TODO:
            e.printStackTrace(); emptyMap()
        }
    }

    private
    fun TransformData.transform(
        w: OutputWriter
    ): Map<TypeDescription?, File> = w.transform(typeDescription!!)
            .saveIn(base)
            .also { maps -> println("maps=$maps") }
}

class ParentOutput2(
    inputs: Map<Input<*>, List<Pair<Entry, OutputWriter>>>,
    file: File
) : Content(file) {

    val inputs = inputs
            .flatMap { (_, v) -> v }
            .filterNot { it.first.isDirectory }

    fun outputs(
        incremental: Boolean
    ): List<Output> = if (incremental) {
        inputs.map { (e, w) ->
            Output(e, file, Status.ADDED, w)
        }
    } else {
        inputs.map { (e, w) ->
            Output(e, file, e.status, w)
        }
    }

    companion object {
        @JvmStatic
        fun root(
            name: String,
            invocation: TransformInvocation,
            inputs: Map<Input<*>, List<Pair<Entry, OutputWriter>>>
        ): ParentOutput2 = ParentOutput2(inputs, invocation.run {
            val contentTypes = inputs.keys.flatMap {
                it.root.contentTypes
            }.toSet()
            val scopes = inputs.keys.flatMap {
                it.root.scopes.filterIsInstance<QualifiedContent.Scope>()
            }.toMutableSet()
            outputProvider.getContentLocation(name, contentTypes, scopes, Format.DIRECTORY)
        })
    }
}
