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
            { input -> input },
            { input -> input.changedFiles(isIncremental) }
        ).flattenValues()

        val entryList = entryMap.flatMap { it.value }
        val transformWriters = writers(entryList)
        val transforms = transformWriters.flatMap { w ->
            w.transform(localClassFileLocator)
        }
        val transformTypes = transforms.flatMap(TransformStep::type)
        val modParents = entryMap.mapValues { (_, entries) ->
            group(entries, transforms) { e, t ->
                e.typeDescription == t.type
            }
        }.filterNotEmpty()

        // Split work
        val unmods = entryMap.map { (i, e) ->
            ParentOutput.copy(invocation, i, e.filterNot {
                transformTypes.contains(it.typeDescription)
            })
        }
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
    fun TransformData.writers(
        entries: List<Entry>
    ) = entries.asSequence()
            .mapNotNull(mapOutputWriter())
            .groupBySecond()
            .map(::TransformWriter)

    private
    fun TransformData.mapOutputWriter() = { input: Entry ->
        outputWriter(input)?.let { w ->
            Pair(input, w)
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

fun <A, B> Collection<Pair<A, B>>.groupByFirst(): Map<A, List<B>> = groupBy(
    { (a, _) -> a },
    { (_, b) -> b }
)

fun <A, B> Collection<Pair<A, B>>.groupBySecond(): Map<B, List<A>> = groupBy(
    { (_, b) -> b },
    { (a, _) -> a }
)

fun <A, B> Sequence<Pair<A, B>>.groupByFirst(): Map<A, List<B>> = groupBy(
    { (a, _) -> a },
    { (_, b) -> b }
)

fun <A, B> Sequence<Pair<A, B>>.groupBySecond(): Map<B, List<A>> = groupBy(
    { (_, b) -> b },
    { (a, _) -> a }
)

fun <K, C : Collection<*>> Map<K, C>.filterNotEmpty() = filter { (_, v) ->
    v.isNotEmpty()
}

fun <K, V> Map<K, Collection<Collection<V>>>.flattenValues() = mapValues { (_, v) ->
    v.flatten()
}

fun <A, B> group(
    collectA: Collection<A>,
    collectB: Collection<B>,
    filter: (A, B) -> Boolean
): List<Pair<A, B>> = collectA.mapNotNull { a ->
    collectB.find { b -> filter(a, b) }
            ?.let { b -> Pair(a, b) }
}
