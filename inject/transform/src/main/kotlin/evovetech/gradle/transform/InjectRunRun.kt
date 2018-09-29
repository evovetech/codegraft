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
        Compound((refInputs + primaryInputs).map {
            it.classFileLocator
        } + bootClassFileLocator)
    }

    private
    val localClassFileLocator: ClassFileLocator by lazy {
        Compound(primaryInputs.map { it.classFileLocator })
    }

    private val outputWriters = writers.toSet()
    private val transformData: TransformData by lazy { TransformData(classFileLocator) }

    override
    fun run() {
        println("inject runrun! start")
        try {
            transformData.run()
        } finally {
            println("inject runrun! complete")
        }
    }

    private
    fun TransformData.run() {
        val invocation = this@InjectRunRun
        val entryMap: Map<Input<*>, List<Entry>> = primaryInputs.groupBy(
            { it },
            { input -> input.changedFiles(isIncremental) }
        ).mapValues { it.value.flatten() }

        val entryList = entryMap.flatMap { it.value }
        val transformWriters = entryList.writers()
        val modEntries = transformWriters.flatMap { w ->
            w.entries()
        }.toSet()

        val transforms = transformWriters.flatMap { w ->
            w.transform(transformData, localClassFileLocator)
        }
        val transformTypes = transforms.flatMap(TransformStep::type)
//        val modTypes = transformTypes + modEntries.flatMap { e -> e.typeDescription!! }

        // Split work
        val unmods = entryMap.map { (i, e) ->
            ParentOutput.copy(invocation, i, e.filterNot {
                transformTypes.contains(it.typeDescription)
            })
        }

        val modParents = entryMap.mapValues { (_, entries) ->
            entries.mapNotNull { e ->
                transforms.find { it.type == e.typeDescription }?.let { t ->
                    Pair(e, t)
                }
            }
        }.filter { it.value.isNotEmpty() }
        val mods = modParents.map { (input, entries) ->
            ParentOutput.transform(invocation, input, entries)
        }

        // Write outputs
        val outputs = (unmods + mods)
                .flatMap { it.outputs(isIncremental) }
        outputs.forEach { output ->
            output.perform(transformData)
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
    fun TransformData.mapOutputWriter() = { input: Entry ->
        Pair(input, outputWriter(input))
    }

    private
    fun List<Entry>.split() = map(transformData.mapOutputWriter())
            .partition { (_, w) -> w == null }
            .let { (copy, mod) ->
                val copies = copy.first()
                val mods = mod.notNull()
                        .groupBySecond()
                Pair(copies, mods)
            }

    private
    fun List<Entry>.writers() = split().second
            .map(::TransformWriter)
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
