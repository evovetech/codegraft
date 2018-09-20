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

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror

data
class MethodBuilder(
    val name: String,
    val init: MethodSpec.Builder.() -> Unit = {}
) {
    fun withName(name: String): MethodBuilder {
        return copy(name = name)
    }

    fun build(): MethodSpec {
        val builder = MethodSpec.methodBuilder(name)
        builder.init()
        return builder.build()
    }
}

fun <T> Collection<Pair<MethodBuilder, T>>.buildUniquePairs() = groupBy {
    it.first.name
}.flatMap { (key, pairs) ->
    val size = pairs.size
    val builders = if (size > 1) {
        var i = 1
        pairs.map { (method, t) ->
            val n = "$key${i++}"
            Pair(method.withName(n), t)
        }
    } else {
        pairs
    }
    builders.map { (method, t) ->
        Pair(method.build(), t)
    }
}

fun Collection<MethodBuilder>.buildUnique() = groupBy {
    it.name
}.flatMap { (key, methods) ->
    val size = methods.size
    val builders = if (size > 1) {
        var i = 1
        methods.map {
            val n = "$key${i++}"
            it.withName(n)
        }
    } else {
        methods
    }
    builders.map { it.build() }
}

data
class ParamBuilder(
    val name: String,
    val type: TypeName,
    val init: ParameterSpec.Builder.() -> Unit = {}
) {
    fun withName(name: String): ParamBuilder {
        return copy(name = name)
    }

    fun build(): ParameterSpec {
        val builder = ParameterSpec.builder(type, name)
        builder.init()
        return builder.build()
    }
}

fun ClassName.buildParameter(
    init: ParameterSpec.Builder.() -> Unit = {}
): ParameterSpec {
    val name = simpleNames().join('_').decapitalize()
    return ParamBuilder(name, this, init)
            .build()
}

fun TypeElement.buildParameter(
    init: ParameterSpec.Builder.() -> Unit = {}
): ParameterSpec = ClassName.get(this)
        .buildParameter(init)

fun TypeMirror.buildParameter(
    name: String,
    init: ParameterSpec.Builder.() -> Unit = {}
): ParameterSpec {
    val type = TypeName.get(this)
    return ParamBuilder(name, type, init)
            .build()
}

fun DeclaredType.buildParameter(
    init: ParameterSpec.Builder.() -> Unit = {}
): ParameterSpec {
    val name = getFieldName()
    return buildParameter(name, init)
}

fun MethodSpec.Builder.addToConstructor(
    fieldSpec: FieldSpec,
    qualifier: AnnotationMirror? = null
): ParameterSpec = ParameterSpec.builder(fieldSpec.type, fieldSpec.name).run {
    qualifier?.let(AnnotationSpec::get)
            ?.let(this::addAnnotation)
    build()
}.also { paramSpec ->
    addStatement("this.\$N = \$N", fieldSpec, paramSpec)
    addParameter(paramSpec)
}

fun TypeSpec.Builder.addFieldSpec(
    fieldType: TypeMirror,
    fieldName: String
): FieldSpec = addFieldSpec(
    TypeName.get(fieldType),
    fieldName
)

fun TypeSpec.Builder.addFieldSpec(
    fieldType: TypeName,
    fieldName: String
): FieldSpec = FieldSpec.builder(fieldType, fieldName)
        .addModifiers(PRIVATE, FINAL)
        .build()
        .apply { addField(this) }
