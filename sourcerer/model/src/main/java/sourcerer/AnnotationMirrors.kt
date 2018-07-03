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

import com.google.auto.common.AnnotationMirrors
import com.google.common.base.Equivalence
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Name

val AnnotationMirror.simpleName: Name
    get() = annotationType.asElement().simpleName

fun AnnotationMirror?.wrap(): Equivalence.Wrapper<AnnotationMirror>? =
    this?.let { AnnotationMirrors.equivalence().wrap(it) }

fun AnnotationMirror.getAnnotationValue(name: String): AnnotationValue =
    AnnotationMirrors.getAnnotationValue(this, name)

inline
fun <reified T : Any> AnnotationMirror.getValue(name: String): T? =
    getAnnotationValue(name).value as? T
