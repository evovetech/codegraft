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

package codegraft.bootstrap

import com.google.auto.common.AnnotationMirrors
import com.google.auto.common.MoreTypes
import com.google.common.base.Equivalence
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import dagger.model.Key
import sourcerer.MethodBuilder
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.lang.model.type.TypeMirror

typealias KeyEquality = Pair<TypeName, Equivalence.Wrapper<AnnotationMirror>>

val Key.type: TypeMirror
    get() = type()
val Key.wrappedType: Equivalence.Wrapper<TypeMirror>
    get() = MoreTypes.equivalence().wrap(type)
val Key.typeName: TypeName
    get() = TypeName.get(type())
val Key.element: Element
    get() = MoreTypes.asElement(type)

val Key.name: String
    get() = element.simpleName.toString()
val Key.fieldName: String
    get() = name.decapitalize()
val Key.getterMethodName: String
    get() = "get${name.capitalize()}"

val Key.qualifier: AnnotationMirror?
    get() = qualifier().orElse(null)
val Key.wrappedQualifier: Equivalence.Wrapper<AnnotationMirror>
    get() = AnnotationMirrors.equivalence().wrap(qualifier)

val Key.equality: KeyEquality
    get() = KeyEquality(typeName, wrappedQualifier)

fun Key.getterMethod(init: MethodSpec.Builder.() -> Unit) =
    MethodBuilder(getterMethodName) {
        init()
        qualifier?.let(AnnotationSpec::get)
                ?.let(this::addAnnotation)
        returns(typeName)
    }
