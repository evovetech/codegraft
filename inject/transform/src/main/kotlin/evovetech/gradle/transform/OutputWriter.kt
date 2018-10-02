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

import codegraft.inject.android.EmptyContentProvider
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

    fun TransformData.transform(
        localClassFileLoader: ClassFileLocator
    ): List<TransformStep> {
        return entries.asSequence()
                .map { it.typeDescription!! }
                // TODO: need to modify injection code in order to do inject parent classes
//                .parent(this, localClassFileLoader)
                .toSet()
                .map(writer::transformStep)
    }

    private
    fun TransformData.skipTypes() = setOf(
        typePool.resolve<EmptyContentProvider>()
    )

    private
    fun Sequence<TypeDescription>.parent(
        transformData: TransformData,
        localClassFileLoader: ClassFileLocator
    ): Sequence<TypeDescription> = transformData.run {
        val skips = skipTypes()
        map { type ->
            val supers = type.toList()
                    .asReversed()
            supers.asSequence()
                    .mapNotNull { t -> resolve(t, localClassFileLoader) }
                    .filterNot { skips.contains(it) }
                    .first()
        }
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
