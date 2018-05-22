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

import com.android.build.api.transform.TransformInvocation
import evovetech.gradle.transform.content.Output
import evovetech.gradle.transform.content.allFiles
import evovetech.gradle.transform.content.classFileLocator
import evovetech.gradle.transform.graph.add
import net.bytebuddy.dynamic.ClassFileLocator.Compound
import net.bytebuddy.pool.TypePool
import net.bytebuddy.pool.TypePool.CacheProvider.Simple
import net.bytebuddy.pool.TypePool.ClassLoading
import net.bytebuddy.pool.TypePool.Default.ReaderMode.FAST
import net.bytebuddy.pool.TypePool.Default.WithLazyResolution
import java.io.File

class GraphRunRun(
    val bootClasspath: () -> List<File>,
    delegate: TransformInvocation
) : RunRun(delegate) {
    val classFileLocator = (refInputs + primaryInputs).map {
        it.classFileLocator
    }.let {
        val boot = bootClasspath().map {
            it.classFileLocator
        }
        Compound(boot + it)
    }
    val typePool: TypePool = WithLazyResolution(
        Simple(),
        classFileLocator,
        FAST,
        ClassLoading.ofBootPath()
    )
    val graph = evovetech.gradle.transform.graph.graph()

    override
    fun run() {
        println("bytebuddy runrun! start")
        try {
            transforms.flatMap { it.output.allFiles() }
                    .forEach(this::write)
            println("graph {")
            graph.edges().forEach {
                println("  $it")
            }
            println("}")
//            println("graph=$graph")
        } finally {
            println("bytebuddy runrun! complete")
        }
    }

    private
    fun write(output: Output) {
        val src = output.input.rel.path
        val copy: () -> Unit = {
            output.copyToDest()
        }
        if (!src.endsWith(CLASS_FILE_EXTENSION)) {
            return copy()
        }

        val typeName = src.replace('/', '.')
                .substring(0, src.length - CLASS_FILE_EXTENSION.length)
        val typeDescription = typePool.describe(typeName).resolve()
        graph.add(typeDescription)
        return copy()
    }
}
