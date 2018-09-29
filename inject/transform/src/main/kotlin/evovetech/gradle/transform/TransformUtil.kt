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

import android.app.Application
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import evovetech.gradle.transform.content.DirInput
import evovetech.gradle.transform.content.JarFileInput
import net.bytebuddy.description.type.TypeDefinition
import net.bytebuddy.description.type.TypeDescription
import net.bytebuddy.implementation.MethodDelegation
import net.bytebuddy.pool.TypePool
import kotlin.reflect.KClass

internal const
val CLASS_FILE_EXTENSION = ".class"

val Collection<TransformInput>.directoryInputs
    get() = flatMap { it.directoryInputs }

val Collection<TransformInput>.jarInputs
    get() = flatMap { it.jarInputs }

val Collection<TransformInput>.all
    get() = flatMap {
        val dirs = it.directoryInputs.map(::DirInput)
        val jars = it.jarInputs.map(::JarFileInput)
        dirs + jars
    }

val TransformInvocation.directoryInputs
    get() = inputs.directoryInputs

val TransformInvocation.jarInputs
    get() = inputs.jarInputs

inline
fun <reified T> TypePool.resolve(): TypeDescription =
    resolve(T::class)

val TypePool.androidApplication: TypeDescription
    get() = resolve<Application>()

fun TypePool.resolve(
    type: KClass<*>
): TypeDescription = describe(type.java.canonicalName)
        .resolve()

fun TypePool.resolve(
    type: TypeDefinition
): TypeDescription = describe(type.typeName)
        .resolve()

inline
fun <reified T> methodDelegation(): MethodDelegation =
    MethodDelegation.to(T::class.java)
