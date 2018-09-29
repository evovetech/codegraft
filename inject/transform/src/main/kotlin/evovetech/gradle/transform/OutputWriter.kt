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

import evovetech.gradle.transform.content.Entry
import net.bytebuddy.description.type.TypeDefinition
import net.bytebuddy.description.type.TypeDescription
import net.bytebuddy.dynamic.ClassFileLocator
import net.bytebuddy.dynamic.DynamicType.Unloaded
import java.io.IOException

interface OutputWriter {
    fun TransformData.canTransform(
        typeDescription: TypeDescription
    ): Boolean

    fun TransformData.transform(
        typeDescription: TypeDescription
    ): Unloaded<*>
}

data
class TransformWriter(
    private val writer: OutputWriter,
    private val entries: List<Entry>
) {
    constructor(
        entry: Map.Entry<OutputWriter, List<Entry>>
    ) : this(entry.key, entry.value)

    fun transform(
        transformData: TransformData,
        localClassFileLoader: ClassFileLocator
    ): List<TransformStep> = transformData.run {
        val types = entries
                .asSequence()
                .map { it.typeDescription!! }
                .map { type ->
                    val supers = type.flatten().asReversed()
                    Pair(type, supers)
                }
                .toList()
                .groupByFirst()
                .mapValues {
                    it.value.flatten()
                            .toSet()
                            .mapNotNull { type -> resolve(type, localClassFileLoader) }
                            .first()
                }
                .map { TransformType(it.key, it.value) }
        val (parents, unmods) = types.split()
        parents.map(writer::transformStep)
    }
}

fun OutputWriter.transformStep(
    type: TypeDescription
) = TransformStep(this, type)

data
class TransformStep(
    val writer: OutputWriter,
    val type: TypeDescription
)

data
class TransformType(
    val type: TypeDescription,
    val parent: TypeDescription
)

fun Collection<TransformType>.split(): Pair<Set<TypeDescription>, List<TypeDescription>> {
    val mods = map { it.parent }
            .toSet()
    val unmods = map { it.type }
            .toSet()
            .filter { !mods.contains(it) }
    return Pair(mods, unmods)
}

fun OutputWriter.transformWriter(
    entries: List<Entry>
) = TransformWriter(this, entries)

fun ClassFileLocator.canResolve(type: TypeDefinition): Boolean = try {
    locate(type.typeName).isResolved
} catch (e: IOException) {
    false
}

fun TransformData.resolve(
    type: TypeDefinition,
    locator: ClassFileLocator
): TypeDescription? = if (locator.canResolve(type)) {
    typePool.resolve(type)
} else {
    null
}
