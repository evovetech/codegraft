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

import codegraft.inject.android.AndroidApplication
import codegraft.inject.android.BootApplication
import codegraft.inject.android.HasApplicationInjector
import dagger.android.HasActivityInjector
import dagger.android.HasBroadcastReceiverInjector
import dagger.android.HasContentProviderInjector
import dagger.android.HasFragmentInjector
import dagger.android.HasServiceInjector
import dagger.android.support.HasSupportFragmentInjector
import net.bytebuddy.build.EntryPoint
import net.bytebuddy.build.EntryPoint.Default.REBASE
import net.bytebuddy.description.modifier.Visibility
import net.bytebuddy.description.type.TypeDescription
import net.bytebuddy.dynamic.DynamicType.Unloaded
import net.bytebuddy.implementation.MethodDelegation

class ApplicationOutputWriter : OutputWriter {
    private val entryPoint: EntryPoint = REBASE

    override
    fun TransformData.canTransform(
        typeDescription: TypeDescription
    ): Boolean = typeDescription.isAssignableTo(resolve<BootApplication<*>>())
                 && typeDescription.isAssignableTo(resolve<AndroidApplication>())

    override
    fun TransformData.transform(
        typeDescription: TypeDescription
    ): Unloaded<out Any> {
        val rawBootType = resolve<BootApplication<*>>().asGenericType().asRawType()
        val bootType = typeDescription.interfaces.filter {
            it.asRawType() == rawBootType
        }.first()
        val componentType = bootType.typeArguments.first()
        return entryPoint.transform(typeDescription)
                .defineMethod("getComponent", componentType, Visibility.PUBLIC)
                .intercept(MethodDelegation.toField("bootstrap"))
                .addInjector<HasApplicationInjector>(this, componentType)
                .addInjector<HasActivityInjector>(this, componentType)
                .addInjector<HasFragmentInjector>(this, componentType)
                .addInjector<HasSupportFragmentInjector>(this, componentType)
                .addInjector<HasServiceInjector>(this, componentType)
                .addInjector<HasContentProviderInjector>(this, componentType)
                .addInjector<HasBroadcastReceiverInjector>(this, componentType)
                .make()
    }
}
