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
import evovetech.gradle.transform.TransformStep
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

val Input<*>.modName: String
    get() = "${root.name}_mod"

abstract
class RelPathContent(
    path: RelPath
) : Content(path.file),
    RelPath by path

data
class Output(
    val input: Entry,
    val root: File,
    val status: Status = input.status,
    val writer: OutputWriter? = null
) : RelPathContent(
    path = RelPath.Factory.create(root, input.relPath.rel)
) {
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

sealed
class ParentOutput(
    val input: Input<*>,
    file: File
) : Content(file) {

    fun outputs(
        incremental: Boolean
    ): List<Output> {
        if (!incremental) {
            FileUtils.deleteRecursivelyIfExists(file)
            return allOutputs()
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
            else -> incrementalOutputs()
        }
    }

    protected abstract
    fun outputs(
        statusOverride: Entry.() -> Status
    ): List<Output>

    private
    fun incrementalOutputs(): List<Output> =
        outputs { status }

    private
    fun allOutputs(): List<Output> =
        outputs { ADDED }

    companion object {
        @JvmStatic
        fun copy(
            invocation: TransformInvocation,
            input: Input<*>,
            entries: List<Entry>
        ): ParentOutput = CopyOutput(input, entries, invocation.getContentLocation(input))

        @JvmStatic
        fun transform(
            invocation: TransformInvocation,
            input: Input<*>,
            transforms: List<Pair<Entry, TransformStep>>
        ): ParentOutput = TransformOutput(input, transforms, invocation.getContentLocation(input))
    }
}

fun TransformInvocation.getContentLocation(input: Input<*>): File {
    val root = input.root
    return outputProvider.getContentLocation(root.name, root.contentTypes, root.scopes, Format.DIRECTORY)
}

private
class CopyOutput(
    input: Input<*>,
    entries: List<Entry>,
    file: File
) : ParentOutput(input, file) {

    val inputs = entries.filterNot { it.isDirectory }

    override
    fun outputs(
        statusOverride: Entry.() -> Status
    ): List<Output> = inputs.map { e ->
        Output(e, file, e.statusOverride())
    }
}

private
class TransformOutput(
    input: Input<*>,
    transforms: List<Pair<Entry, TransformStep>>,
    file: File
) : ParentOutput(input, file) {
    val transforms = transforms
            .filterNot { it.first.isDirectory }

    override
    fun outputs(
        statusOverride: Entry.() -> Status
    ): List<Output> = transforms.map { (e, t) ->
        Output(e, file, e.statusOverride(), t.writer)
    }
}
