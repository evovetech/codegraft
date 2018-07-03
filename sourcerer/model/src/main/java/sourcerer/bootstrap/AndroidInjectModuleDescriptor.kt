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

package sourcerer.bootstrap

import com.google.auto.common.MoreTypes
import com.google.common.base.Equivalence
import com.google.common.collect.ImmutableList
import dagger.internal.codegen.getTypeListValue
import sourcerer.getAnnotationMirror
import sourcerer.getValue
import sourcerer.inject.AndroidInject
import javax.inject.Inject
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

class AndroidInjectModuleDescriptor(
    val element: TypeElement,

    val annotationMirror: AnnotationMirror,

    val generatedName: String,

    val includes: ImmutableList<TypeMirror>
) {
    val type: TypeMirror = element.asType()

    val typeWrapper: Equivalence.Wrapper<TypeMirror> =
        MoreTypes.equivalence().wrap(type)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AndroidInjectModuleDescriptor

        if (typeWrapper != other.typeWrapper) return false

        return true
    }

    override fun hashCode(): Int {
        return typeWrapper.hashCode()
    }

    class Factory
    @Inject constructor() {
        fun create(element: TypeElement): AndroidInjectModuleDescriptor {
            val annotationMirror = element.getAnnotationMirror<AndroidInject>()!!
            val generatedName = annotationMirror.getValue<String>("generatedName")
                    .orEmpty()
            val includes = annotationMirror.getTypeListValue("includes")
            return AndroidInjectModuleDescriptor(
                element = element,
                annotationMirror = annotationMirror,
                generatedName = generatedName,
                includes = includes
            )
        }
    }
}
