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

import com.android.build.api.transform.TransformInvocation
import evovetech.gradle.transform.content.Entry
import evovetech.gradle.transform.content.Input
import evovetech.gradle.transform.content.ParentOutput
import evovetech.gradle.transform.content.ParentOutput2
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
            val inputs = primaryInputs
                    .groupBy { it }
                    .mapValues { (input, _) ->
                        val all = input.changedFiles(isIncremental).map { e ->
                            val writer = transformData.outputWriter(e)
                            Pair(e, writer)
                        }
                        val copies = all.filter { it.second == null }
                                .map { it.first }
                        val transforms = all.filter { it.second != null }
                        Pair(copies, transforms)
                    }

            // unmods
            val unmods = inputs.mapValues { (_, pairs) -> pairs.first }
                    .filter { it.value.isNotEmpty() }
                    .map { (i, e) -> ParentOutput.root(this, i, e) }
                    .flatMap { it.outputs(isIncremental) }
            // mods
            val mods = inputs.mapValues { (_, pairs) ->
                pairs.second.mapNotNull { (e, w) ->
                    w?.let { Pair(e, it) }
                }
            }.filter { it.value.isNotEmpty() }

            // TODO: group by writer
            unmods.forEach { output ->
                output.perform(transformData)
            }

            ParentOutput2.root("mods", this, mods)
                    .outputs(isIncremental)
                    .forEach { output ->
                        output.perform(transformData)
                    }
        } finally {
            println("inject runrun! complete")
        }
    }

    private
    fun TransformData.outputWriter(input: Entry): OutputWriter? = try {
        input.typeDescription?.let { type ->
            outputWriters.firstOrNull { writer -> writer.canTransform(type) }
        }
    } catch (exception: Throwable) {
        exception.printStackTrace()
        null
    }

    private
    fun Input<*>.map(
        isIncremental: Boolean
    ) = changedFiles(isIncremental)
            .map { e -> val w = transformData.outputWriter(e); Pair(e, w) }
            .partition { (_, w) -> w == null }
            .let { (copy, mod) ->
                val copies = copy.first()
                val mods = mod.notNull()
                        .groupBySecond()
                Pair(copies, mods)
            }
}

fun <A : Any, B : Any> Collection<Pair<A?, B?>>.notNull(): List<Pair<A, B>> = mapNotNull { (a, b) ->
    if (a == null || b == null) {
        null
    } else {
        Pair(a, b)
    }
}

fun <T> Collection<Pair<T, *>>.first(): List<T> = map { (f, _) -> f }
fun <T> Collection<Pair<*, T>>.second(): List<T> = map { (_, s) -> s }

fun <A, B> Collection<Pair<A, B>>.groupByFirst(): Map<A, List<B>> = groupBy { (a, _) -> a }
        .mapValues { (_, v) -> v.second() }

fun <A, B> Collection<Pair<A, B>>.groupBySecond(): Map<B, List<A>> = groupBy { (_, b) -> b }
        .mapValues { (_, v) -> v.first() }
