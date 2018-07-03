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

import com.google.auto.common.AnnotationMirrors.getAnnotatedAnnotations
import com.google.auto.common.MoreElements.isAnnotationPresent
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import sourcerer.bootstrap.toImmutableSet
import javax.inject.Inject
import javax.inject.Qualifier
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.ElementFilter.constructorsIn

const val BOOTSTRAP_DEPENDENCIES_ATTRIBUTE = "bootstrapDependencies"
const val BOOTSTRAP_MODULES_ATTRIBUTE = "bootstrapModules"
const val APPLICATION_MODULES_ATTRIBUTE = "applicationModules"
const val AUTO_INCLUDE_ATTRIBUTE = "autoInclude"
const val FLATTEN_ATTRIBUTE = "flatten"
const val DEPENDENCIES_ATTRIBUTE = "dependencies"
const val MODULES_ATTRIBUTE = "modules"

val AnnotationMirror.bootstrapComponentDependencies: ImmutableList<TypeMirror>
    get() = getTypeListValue(BOOTSTRAP_DEPENDENCIES_ATTRIBUTE)

val Element.qualifier: AnnotationMirror?
    get() {
        val qualifierAnnotations = qualifiers
        return when (qualifierAnnotations.size) {
            0 -> null
            1 -> qualifierAnnotations.first()
            else -> throw IllegalArgumentException(
                toString() + " was annotated with more than one @Qualifier annotation"
            )
        }
    }

val Element.qualifiers: ImmutableSet<out AnnotationMirror>
    get() = getAnnotatedAnnotations(this, Qualifier::class.java)

/** Returns the constructors in `type` that are annotated with [Inject].  */
val TypeElement.injectedConstructors: ImmutableSet<ExecutableElement>
    get() = constructorsIn(enclosedElements)
            .filter { constructor -> isAnnotationPresent(constructor, Inject::class.java) }
            .toImmutableSet()
