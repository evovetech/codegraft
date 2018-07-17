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

package codegraft.bootstrap

import com.google.auto.common.MoreTypes
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import dagger.model.Key
import sourcerer.MethodBuilder
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.lang.model.type.TypeMirror

val Key.type: TypeMirror
    get() = type()
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

fun Key.getterMethod(init: MethodSpec.Builder.() -> Unit) =
    MethodBuilder(getterMethodName) {
        init()
        qualifier?.let(AnnotationSpec::get)
                ?.let(this::addAnnotation)
        returns(typeName)
    }
