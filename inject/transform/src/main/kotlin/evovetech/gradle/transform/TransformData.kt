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

import evovetech.codegen.BootstrapMethods
import evovetech.gradle.transform.content.Entry
import evovetech.gradle.transform.content.Output
import net.bytebuddy.ByteBuddy
import net.bytebuddy.ClassFileVersion
import net.bytebuddy.build.EntryPoint
import net.bytebuddy.description.type.TypeDescription
import net.bytebuddy.dynamic.ClassFileLocator
import net.bytebuddy.dynamic.DynamicType
import net.bytebuddy.dynamic.DynamicType.Builder
import net.bytebuddy.dynamic.DynamicType.Unloaded
import net.bytebuddy.dynamic.scaffold.inline.MethodNameTransformer.Suffixing
import net.bytebuddy.pool.TypePool
import net.bytebuddy.pool.TypePool.CacheProvider.Simple
import net.bytebuddy.pool.TypePool.ClassLoading
import net.bytebuddy.pool.TypePool.Default.ReaderMode.FAST
import net.bytebuddy.pool.TypePool.Default.WithLazyResolution

class TransformData(
    private val classFileLocator: ClassFileLocator,
    private val classFileVersion: ClassFileVersion = ClassFileVersion.JAVA_V7
) {
    val typePool: TypePool by lazy {
        WithLazyResolution(
            Simple(),
            classFileLocator,
            FAST,
            ClassLoading.ofBootPath()
        )
    }
    val methodTransformer = Suffixing("original")

    val Entry.src: String
        get() = path
    val Entry.typeName: String?
        get() = if (src.endsWith(CLASS_FILE_EXTENSION)) {
            src.replace('/', '.')
                    .substring(0, src.length - CLASS_FILE_EXTENSION.length)
        } else {
            null
        }
    val Entry.typeDescription: TypeDescription?
        get() = typeName?.let { type -> typePool.describe(type).resolve() }

    val Output.src: String
        get() = input.src
    val Output.typeName: String?
        get() = input.typeName
    val Output.typeDescription: TypeDescription?
        get() = input.typeDescription

    fun EntryPoint.newByteBuddy(): ByteBuddy =
        byteBuddy(classFileVersion)

    fun EntryPoint.transform(
        typeDescription: TypeDescription
    ): Builder<*> = transform(
        typeDescription,
        newByteBuddy(),
        classFileLocator,
        methodTransformer
    )

    fun OutputWriter.canTransform(
        typeDescription: TypeDescription
    ): Boolean = try {
        this@TransformData.canTransform(typeDescription)
    } catch (_: Throwable) {
        false
    }

    fun OutputWriter.transform(
        typeDescription: TypeDescription
    ): Unloaded<*> {
        return this@TransformData.transform(typeDescription)
    }
}

inline
fun <reified T> TransformData.resolve(): TypeDescription =
    typePool.resolve<T>()

inline
fun <reified T> DynamicType.Builder<*>.addInjector(
    transformData: TransformData,
    componentType: TypeDescription.Generic
): DynamicType.Builder<*> {
    val hasInjectorType = transformData.resolve<T>()
    if (componentType.asErasure().isAssignableTo(hasInjectorType)) {
        println("$componentType is assignable to $hasInjectorType")
        return implement(hasInjectorType)
                .intercept(methodDelegation<BootstrapMethods>())
    }
    return this
}
