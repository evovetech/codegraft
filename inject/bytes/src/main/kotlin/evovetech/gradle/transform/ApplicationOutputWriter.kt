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

import evovetech.codegen.LogMethod
import net.bytebuddy.build.EntryPoint
import net.bytebuddy.build.EntryPoint.Default.REBASE
import net.bytebuddy.description.modifier.Visibility
import net.bytebuddy.description.type.TypeDescription
import net.bytebuddy.dynamic.DynamicType.Unloaded
import net.bytebuddy.implementation.MethodDelegation
import net.bytebuddy.matcher.ElementMatchers
import sourcerer.inject.android.AndroidApplication
import sourcerer.inject.android.BootApplication
import sourcerer.inject.android.HasActivityInjector
import sourcerer.inject.android.HasApplicationInjector
import sourcerer.inject.android.HasBroadcastReceiverInjector
import sourcerer.inject.android.HasContentProviderInjector
import sourcerer.inject.android.HasServiceInjector

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
        var transform = entryPoint.transform(typeDescription)

        transform = transform
                .defineMethod("getComponent", componentType, Visibility.PUBLIC)
                .intercept(MethodDelegation.toField("bootstrap"))
        transform = addInjector<HasApplicationInjector>(componentType, transform)
        transform = addInjector<HasActivityInjector>(componentType, transform)
        transform = addInjector<HasServiceInjector>(componentType, transform)
        transform = addInjector<HasContentProviderInjector>(componentType, transform)
        transform = addInjector<HasBroadcastReceiverInjector>(componentType, transform)

        return transform.defineField("defined", String::class.java).value("one")
                .method(ElementMatchers.named("onCreate")).intercept(methodDelegation<LogMethod>())
                .make()
    }
}
