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

package sourcerer

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import javax.annotation.processing.Filer

/**
 * Created by layne on 3/8/18.
 */

interface Klass {
    val rawType: ClassName
    val pkg: Package
        get() = rawType.toPackage()
    val name: String
        get() = rawType.name
    val canonicalName: String
        get() = rawType.toString()
    val javaName: String
        get() = "$canonicalName.java"
}

fun MutableList<String>.addTo(index: Int, value: String): String {
    val cur = get(index)
    return set(index, cur + value)
}

fun MutableList<String>.addToEnd(value: String): String {
    return addTo(lastIndex, value)
}

operator fun Klass.plus(name: String) =
    (rawType + name).toKlass()

fun String.toKlass(): Klass = ClassName.bestGuess(this)
        .toKlass()

fun ClassName.toKlass(): Klass = DefaultKlass(this)

fun ClassName.append(name: String, separator: String = ""): ClassName = if (name.isEmpty()) {
    this
} else {
    val add = if (separator.isEmpty()) {
        name
    } else {
        "$separator$name"
    }
    val names = simpleNames()
            .toMutableList()
            .apply { addToEnd(add) }
    val simpleName = names.first()
    val simpleNames = names.drop(1)
            .toTypedArray()
    ClassName.get(packageName(), simpleName, *simpleNames)
}

operator fun ClassName.plus(name: String): ClassName =
    append(name = name)

fun Klass.writeTo(filer: Filer, text: String) = filer
        .createSourceFile(canonicalName)
        .openWriter()
        .buffered()
        .use {
            it.write(text)
            it.flush()
        }

fun Klass.parameterizedType(
    vararg types: TypeName
) = ParameterizedTypeName.get(rawType, *types)!!

fun Klass.annotationBuilder() =
    AnnotationSpec.builder(rawType)!!

fun Klass.classBuilder() =
    TypeSpec.classBuilder(name)!!

fun Klass.interfaceBuilder() =
    TypeSpec.interfaceBuilder(name)!!

fun Klass.annotation() = annotationBuilder()
        .build()!!

fun Klass.nestedBuilder() = rawType.nestedClass("Builder")
        .toKlass()

inline fun Klass.annotation(init: AnnotationSpec.Builder.() -> Unit): AnnotationSpec {
    val builder = annotationBuilder()
    builder.init()
    return builder.build()
}

open class DefaultKlass(
    override val rawType: ClassName
) : Klass {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DefaultKlass

        if (rawType != other.rawType) return false

        return true
    }

    override fun hashCode(): Int {
        return rawType.hashCode()
    }
}
