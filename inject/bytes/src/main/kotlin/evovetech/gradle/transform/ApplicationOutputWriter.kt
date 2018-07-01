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

import evovetech.codegen.OnCreate
import net.bytebuddy.build.EntryPoint
import net.bytebuddy.build.EntryPoint.Default.REBASE
import net.bytebuddy.description.type.TypeDescription
import net.bytebuddy.dynamic.DynamicType.Unloaded
import net.bytebuddy.matcher.ElementMatchers

class ApplicationOutputWriter : OutputWriter {
    private val entryPoint: EntryPoint = REBASE

    override
    fun TransformData.canTransform(
        typeDescription: TypeDescription
    ): Boolean = typeDescription.isAssignableTo(typePool.androidApplication)

    override
    fun TransformData.transform(
        typeDescription: TypeDescription
    ): Unloaded<out Any> = entryPoint.transform(typeDescription)
            .defineProperty("defined", String::class.java)
            .field(ElementMatchers.named("defined")).value("one")
            .method(ElementMatchers.named("onCreate")).intercept(methodDelegation<OnCreate>())
            .make()
}