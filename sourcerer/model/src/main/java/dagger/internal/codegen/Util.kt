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

package dagger.internal.codegen

import com.google.auto.common.MoreElements
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier.ABSTRACT
import javax.lang.model.element.TypeElement
import javax.lang.model.util.ElementFilter

fun simpleVariableName(typeElement: TypeElement): String =
    SourceFiles.simpleVariableName(typeElement)

fun componentCanMakeNewInstances(typeElement: TypeElement): Boolean =
    Util.componentCanMakeNewInstances(typeElement)

internal
fun SourcererElements.isComponentContributionMethod(method: ExecutableElement): Boolean {
    return ComponentDescriptor.isComponentContributionMethod(this, method)
}

fun <T> Collection<T>.toImmutableList(): ImmutableList<T> {
    return ImmutableList.copyOf(this)
}

fun <T> Collection<T>.toImmutableSet(): ImmutableSet<T> {
    return ImmutableSet.copyOf(this)
}

fun <K, V> Map<K, V>.toImmutableMap(): ImmutableMap<K, V> {
    return ImmutableMap.copyOf(this)
}

inline
fun <reified E> immutableSet(init: ImmutableSet.Builder<E>.() -> Unit): ImmutableSet<E> {
    val builder = ImmutableSet.builder<E>()
    builder.init()
    return builder.build()
}

internal
fun SourcererElements.abstractMethods(
    typeElement: TypeElement
) = ElementFilter.methodsIn(getAllMembers(typeElement))
        .filter(MoreElements.hasModifiers<ExecutableElement>(ABSTRACT)::apply)
