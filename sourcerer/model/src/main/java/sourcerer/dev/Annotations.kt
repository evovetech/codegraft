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

package sourcerer.dev

import com.google.common.collect.ImmutableList
import sourcerer.dev.MoreAnnotationMirrors.getTypeListValue
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.type.TypeMirror

const val BOOTSTRAP_DEPENDENCIES_ATTRIBUTE = "bootstrapDependencies"
const val BOOTSTRAP_MODULES_ATTRIBUTE = "bootstrapModules"
const val APPLICATION_MODULES_ATTRIBUTE = "applicationModules"
const val DEPENDENCIES_ATTRIBUTE = "dependencies"
const val MODULES_ATTRIBUTE = "modules"

fun getBootstrapComponentDependencies(
    componentAnnotation: AnnotationMirror
): ImmutableList<TypeMirror> {
    checkNotNull(componentAnnotation)
    return getTypeListValue(componentAnnotation, BOOTSTRAP_DEPENDENCIES_ATTRIBUTE)
}
