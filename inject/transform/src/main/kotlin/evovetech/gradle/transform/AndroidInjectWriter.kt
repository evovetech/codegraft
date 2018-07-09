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

import evovetech.codegen.AndroidInjectMethods
import evovetech.codegen.Transformed
import net.bytebuddy.build.EntryPoint
import net.bytebuddy.build.EntryPoint.Default.REBASE
import net.bytebuddy.description.annotation.AnnotationDescription
import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.description.type.TypeDescription
import net.bytebuddy.dynamic.DynamicType
import net.bytebuddy.dynamic.DynamicType.Unloaded
import net.bytebuddy.implementation.MethodDelegation
import net.bytebuddy.matcher.ElementMatcher.Junction
import kotlin.reflect.KClass
import sourcerer.inject.AndroidInject

class AndroidInjectWriter : OutputWriter {
    private val entryPoint: EntryPoint = REBASE

    override
    fun TransformData.canTransform(
        typeDescription: TypeDescription
    ): Boolean = typeDescription.declaredAnnotations.isAnnotationPresent(AndroidInject::class.java)
                 && typeDescription.androidInjectType.run {
        when (this) {
            null -> false
            else -> !typeDescription.isTransformed()
        }
    }

    override
    fun TransformData.transform(
        typeDescription: TypeDescription
    ): Unloaded<out Any> = typeDescription.androidInjectType!!.run {
        entryPoint.transform(typeDescription)
                .inject()
                .make()
    }
}

enum
class AndroidInjectType(
    val componentType: Class<*>,
    val methodFilter: Junction<MethodDescription>,
    val methodDelegation: MethodDelegation = methodDelegation<AndroidInjectMethods>()
) {
    Activity(android.app.Activity::class.java, activityOnCreate()),
    SupportFragment(android.support.v4.app.Fragment::class.java, fragmentOnActivityCreated()),
    Fragment(android.app.Fragment::class.java, fragmentOnActivityCreated());

    fun matches(typeDescription: TypeDescription): Boolean {
        return typeDescription.isAssignableTo(componentType)
    }

    fun TypeDescription.isTransformed(): Boolean {
        return declaredAnnotations.isAnnotationPresent(Transformed::class.java).also { isPresent ->
            println("$this is transformed? = $isPresent")
        }
    }

    fun DynamicType.Builder<*>.inject(): DynamicType.Builder<*> {
        return method(methodFilter)
                .intercept(methodDelegation)
                .annotateType(annotationDescription<Transformed>())
    }

    companion object {
        @JvmStatic
        fun getType(
            typeDescription: TypeDescription
        ): AndroidInjectType? = values().find { type ->
            type.matches(typeDescription)
        }
    }
}

inline
fun <reified T : Annotation> annotationDescription(): AnnotationDescription =
    T::class.annotationDescription

val <T : Annotation> KClass<T>.annotationDescription: AnnotationDescription
    get() = AnnotationDescription.Builder.ofType(this.java)
            .build().apply {
                println("annotation description = $this")
            }

val TypeDescription.androidInjectType: AndroidInjectType?
    get() = AndroidInjectType.getType(this)
