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
