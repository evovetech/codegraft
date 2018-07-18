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

import codegraft.inject.android.AndroidApplication
import codegraft.inject.android.BootApplication
import codegraft.inject.android.HasApplicationInjector
import dagger.android.HasActivityInjector
import dagger.android.HasBroadcastReceiverInjector
import dagger.android.HasContentProviderInjector
import dagger.android.HasFragmentInjector
import dagger.android.HasServiceInjector
import dagger.android.support.HasSupportFragmentInjector
import evovetech.codegen.LogMethod
import net.bytebuddy.build.EntryPoint
import net.bytebuddy.build.EntryPoint.Default.REBASE
import net.bytebuddy.description.modifier.Visibility
import net.bytebuddy.description.type.TypeDescription
import net.bytebuddy.dynamic.DynamicType.Unloaded
import net.bytebuddy.implementation.MethodDelegation
import net.bytebuddy.matcher.ElementMatchers

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
                .method(ElementMatchers.named("onCreate")).intercept(methodDelegation<LogMethod>())
                .make()
    }
}
