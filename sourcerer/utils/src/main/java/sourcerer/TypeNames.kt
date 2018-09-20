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

import com.squareup.javapoet.ArrayTypeName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeVariableName
import com.squareup.javapoet.WildcardTypeName
import javax.lang.model.type.TypeMirror

fun TypeMirror.getFieldName(): String = TypeName.get(this)
        .getFieldName()

fun TypeName.getFieldName(): String {
    val boxedPrimitive: ClassName? = when {
        isPrimitive -> box() as? ClassName
        else -> null
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
    types.add(rawType)
    val fieldName = types.joinToString(separator = "") {
        it.getFieldName().capitalize()
    }
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
