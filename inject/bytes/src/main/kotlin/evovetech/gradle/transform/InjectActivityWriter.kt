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

import android.app.Activity
import evovetech.codegen.ActivityInjections
import net.bytebuddy.build.EntryPoint
import net.bytebuddy.build.EntryPoint.Default.REBASE
import net.bytebuddy.description.type.TypeDescription
import net.bytebuddy.description.type.TypeVariableToken
import net.bytebuddy.dynamic.DynamicType.Unloaded
import net.bytebuddy.dynamic.Transformer
import sourcerer.inject.InjectActivity

class InjectActivityWriter : OutputWriter {
    private val entryPoint: EntryPoint = REBASE

    override
    fun TransformData.canTransform(
        typeDescription: TypeDescription
    ): Boolean = typeDescription.declaredAnnotations.isAnnotationPresent(InjectActivity::class.java)

    override
    fun TransformData.transform(
        typeDescription: TypeDescription
    ): Unloaded<out Any> {
        var transform = entryPoint.transform(typeDescription)

        val rawInjectType = typePool.describe(ActivityInjections::class.java.simpleName).resolve()
        entryPoint.newByteBuddy()
                .subclass(rawInjectType)
                .transform({
                    it.represents(Activity::class.java)
                }, Transformer.Compound.)

//        val wrap = entryPoint.newByteBuddy()
//                .subclass(ActivityInjections::class.java)
//                .
        typeDescription.asGenericType()

        // TODO: add @Inject field for fragment provider
        return transform
//                .method(definedMethod {
//                    it.name == "onCreate"
//                    && it.declaringType == typeDescription
//                    && it.parameters.size == 1
//                    && it.parameters.first().type.represents(Bundle::class.java)
//                })
//                .method(ElementMatchers.named("onCreate")).intercept(MethodDelegation.to())
//                .transform(Transformer.ForMethod.withModifiers(Visibility.PUBLIC))
                .make()
    }
}
