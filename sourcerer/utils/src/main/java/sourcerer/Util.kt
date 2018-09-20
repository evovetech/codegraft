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

package sourcerer

import com.google.auto.common.MoreElements
import com.google.common.base.Joiner
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import com.squareup.javapoet.ClassName
import sourcerer.processor.Env
import sourcerer.processor.EnvProcessor
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier.ABSTRACT
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.MirroredTypesException
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.ElementFilter
import javax.lang.model.util.Elements
import kotlin.reflect.KClass

/**
 * Created by layne on 2/21/18.
 */

typealias ExactSet<T> = Set<@JvmSuppressWildcards T>

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
fun Elements.abstractMethods(
    typeElement: TypeElement
) = ElementFilter.methodsIn(getAllMembers(typeElement))
        .filter(MoreElements.hasModifiers<ExecutableElement>(ABSTRACT)::apply)

fun CharSequence?.ifNotEmpty(block: (CharSequence) -> Unit) {
    if (!isNullOrEmpty()) {
        block(this!!)
    }
}

fun Elements.typeOf(clazz: KClass<*>): TypeElement {
    return getTypeElement(clazz.java.canonicalName)
}

fun Elements.typeOf(typeMirror: TypeMirror): TypeElement {
    return getTypeElement(typeMirror.toString())
}

fun Elements.typeOf(block: () -> KClass<*>): TypeElement = try {
    typeOf(block())
} catch (e: MirroredTypeException) {
    typeOf(e.typeMirror)
}

fun Elements.typesOf(block: () -> Array<KClass<*>>): List<TypeElement> = try {
    val classes = block()
    classes.map { typeOf(it) }
} catch (e: MirroredTypesException) {
    e.typeMirrors.map { typeOf(it) }
}

fun List<String>.join(sep: Char = '.'): String {
    return Joiner.on(sep)
            .join(this)
}

fun Env.typeOf(block: () -> KClass<*>) =
    elements().typeOf(block)

fun Env.typesOf(block: () -> Array<KClass<*>>) =
    elements().typesOf(block)

fun Env.logOne(key: String): (Any) -> Unit = {
    log("%s = %s", key, it)
}

fun EnvProcessor<*>.logOne(key: String) =
    env().logOne(key)

val ClassName.name: String
    get() = simpleNames()
            .join()

val ClassName.metaFile: MetaInf.File
    get() = MetaInf.create(packageName().replace('.', '/'))
            .file(name)

val ClassName.qualifiedName: String
    get() {
        val packageName = packageName()
        if (packageName.isEmpty()) {
            return simpleName()
        }
        val names = ArrayList(simpleNames())
        names.add(0, packageName)
        return Joiner.on('.')
                .join(names)
    }

fun typeName(packageName: String, className: String): ClassName {
    if (className.isEmpty()) {
        throw IllegalArgumentException("empty className")
    }
    val index = className.indexOf('.')
    if (index == -1) {
        return ClassName.get(packageName, className)
    }

    // Add the class names, like "Map" and "Entry".
    val parts = className.substring(index + 1).split("\\.".toRegex()).toTypedArray()
    return ClassName.get(packageName, className, *parts)
}

inline fun <reified A : Annotation> ImmutableSet.Builder<String>.add() =
    add(A::class.java.canonicalName)!!

fun ImmutableSet.Builder<String>.add(klass: Klass) =
    add(klass.canonicalName)!!
