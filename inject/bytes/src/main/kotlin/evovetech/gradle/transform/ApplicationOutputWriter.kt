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
import net.bytebuddy.pool.TypePool

class ApplicationOutputWriter : OutputWriter {
    private val entryPoint: EntryPoint = REBASE

    override
    fun TransformData.canTransform(
        typeDescription: TypeDescription
    ): Boolean = typeDescription.isAssignableTo(typePool.BootApplication)

    override
    fun TransformData.transform(
        typeDescription: TypeDescription
    ): Unloaded<out Any> {
        val rawBootType = typePool.BootApplication.asGenericType().asRawType()
        println("rawBootType = $rawBootType")
        val bootType = typeDescription.interfaces.filter {
            it.asRawType() == rawBootType
        }.first()!!
        println("bootType = $bootType")
        val componentType = bootType.typeArguments.first()!!
        println("componentType = $componentType")

        var transform = entryPoint.transform(typeDescription)

        transform = transform
                .defineMethod("getComponent", componentType, Visibility.PUBLIC)
                .intercept(MethodDelegation.toField("bootstrap"))

//        transform.method(ElementMatchers.named("getBootstrap")).in

        val hasApplicationInjector = typePool.HasApplicationInjector
        if (componentType.asErasure().isAssignableTo(hasApplicationInjector)) {
            println("$componentType is assignable to $hasApplicationInjector")
//            transform = transform.implement(hasApplicationInjector)
//                    .intercept(MethodDelegation.toField("bootstrap"))
//                    .intercept(object: Implementation{
//
//                    })
        }

        val hasActivityInjector = typePool.HasActivityInjector
        if (componentType.asErasure().isAssignableTo(hasActivityInjector)) {

        }

//        va

//            .implement()
        return transform.defineField("defined", String::class.java).value("one")
                .method(ElementMatchers.named("onCreate")).intercept(methodDelegation<LogMethod>())
                .make()
    }
}

val TypePool.BootApplication: TypeDescription
    get() = resolve<sourcerer.inject.android.BootApplication<*>>()
val TypePool.HasApplicationInjector: TypeDescription
    get() = resolve<sourcerer.inject.android.HasApplicationInjector>()
val TypePool.HasActivityInjector: TypeDescription
    get() = resolve<sourcerer.inject.android.HasActivityInjector>()
