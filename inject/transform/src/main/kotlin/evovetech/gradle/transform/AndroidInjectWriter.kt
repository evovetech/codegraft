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

import codegraft.inject.AndroidInject
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
    SupportFragment(android.support.v4.app.Fragment::class.java, fragmentOnAttach()),
    Fragment(android.app.Fragment::class.java, fragmentOnAttach()),
    Service(android.app.Service::class.java, serviceOnCreate());

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
