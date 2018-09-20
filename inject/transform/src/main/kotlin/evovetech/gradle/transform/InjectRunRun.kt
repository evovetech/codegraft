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

package evovetech.gradle.transform

import com.android.build.api.transform.Status.NOTCHANGED
import com.android.build.api.transform.Status.REMOVED
import com.android.build.api.transform.TransformInvocation
import com.android.utils.FileUtils
import evovetech.gradle.transform.content.Output
import evovetech.gradle.transform.content.classFileLocator
import net.bytebuddy.dynamic.ClassFileLocator
import net.bytebuddy.dynamic.ClassFileLocator.Compound
import java.io.File

class InjectRunRun(
    bootClasspath: () -> List<File>,
    delegate: TransformInvocation,
    vararg writers: OutputWriter
) : RunRun(delegate) {
    constructor(
        bootClasspath: () -> List<File>,
        delegate: TransformInvocation
    ) : this(
        bootClasspath, delegate,
        ApplicationOutputWriter()
    )

    private
    val bootClassFileLocator by lazy {
        Compound(bootClasspath().map { it.classFileLocator })
    }

    private
    val classFileLocator: ClassFileLocator by lazy {
        (refInputs + primaryInputs).map {
            it.classFileLocator
        }.let {
            Compound(it + bootClassFileLocator)
        }
    }

    private val outputWriters = writers.toSet()
    private val transformData: TransformData by lazy { TransformData(classFileLocator) }

    override
    fun run() {
        println("inject runrun! start")
        try {
            transforms.flatMap { it.output.outputs(isIncremental) }
                    .forEach(this::write)
        } finally {
            println("inject runrun! complete")
        }
    }

    private
    fun write(output: Output): Unit = transformData.run {
        val copy: () -> Unit = {
            output.copyToDest()
        }

        val input = output.input
        val status = input.status
        when (status) {
            NOTCHANGED -> {
                // nothing
                return
            }
            REMOVED -> {
                FileUtils.deleteRecursivelyIfExists(output.file)
                return
            }
            else -> {
                // continue
            }
        }

        try {
            val type = output.typeDescription
                       ?: return copy()
            outputWriters.find { writer -> writer.canTransform(type) }
                    ?.transform(type)
                    ?.saveIn(output.base)
                    ?.also { maps -> println("maps=$maps") }
            ?: return copy()
        } catch (exception: Throwable) {
            exception.printStackTrace()
            return copy()
        }
    }
}
