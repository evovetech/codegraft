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

import com.google.auto.common.MoreTypes.asPrimitiveType
import com.squareup.javapoet.ArrayTypeName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeVariableName
import com.squareup.javapoet.WildcardTypeName
import javax.lang.model.type.TypeMirror

internal
typealias SourcererTypes = DaggerTypes

internal
fun SourcererTypes.boxedType(
    type: TypeMirror
): TypeMirror = if (type.kind.isPrimitive) {
    boxedClass(asPrimitiveType(type)).asType()
} else {
    type
}

internal inline
fun <reified T> SourcererTypes.rewrapType(
    type: TypeMirror
): TypeMirror = rewrapType(type, T::class.java)

internal inline
fun <reified T> SourcererTypes.wrapType(
    type: TypeMirror
): TypeMirror = wrapType(type, T::class.java)

fun TypeMirror.getFieldName(): String = TypeName.get(this)
        .getFieldName()

fun TypeName.getFieldName(): String {
    var boxedPrimitive: TypeName? = null
    if (isPrimitive) {
        boxedPrimitive = box()
    } else if (isBoxedPrimitive) {
        boxedPrimitive = this
    }
    boxedPrimitive?.let {
        return it.getFieldName()
    }
    return when (this) {
        is ClassName -> getFieldName()
        is ParameterizedTypeName -> getFieldName()
        is ArrayTypeName -> getFieldName()
        is TypeVariableName -> name.capitalize()
        is WildcardTypeName -> getFieldName()
        else -> {
            throw IllegalArgumentException("can't find typename of ${this::class.java.canonicalName}")
        }
    }
}

fun ClassName.getFieldName(): String {
    return simpleName().decapitalize()
}

fun ParameterizedTypeName.getFieldName(): String {
    val types = typeArguments.toMutableList()
    types.reverse()
    val fieldName = types.joinToString(separator = "") {
        it.getFieldName().capitalize()
    } + rawType.getFieldName().capitalize()
    return fieldName.decapitalize()
}

fun ArrayTypeName.getFieldName(): String {
    val arrayType = componentType.getFieldName()
    return "${arrayType}Array"
}

fun WildcardTypeName.getFieldName(): String {
    if (!lowerBounds.isEmpty()) {
        return lowerBounds.first().getFieldName()
    }
    return upperBounds.first().getFieldName()
}
