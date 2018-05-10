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

package evovetech.gradle.transform

import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import evovetech.gradle.transform.content.DirInput
import evovetech.gradle.transform.content.JarFileInput

internal const
val CLASS_FILE_EXTENSION = ".class"

val Collection<TransformInput>.directoryInputs
    get() = flatMap { it.directoryInputs }

val Collection<TransformInput>.jarInputs
    get() = flatMap { it.jarInputs }

val Collection<TransformInput>.all
    get() = flatMap {
        val dirs = it.directoryInputs.map {
            DirInput(it)
        }
        val jars = it.jarInputs.map {
            JarFileInput(it)
        }
        dirs + jars
    }

val TransformInvocation.directoryInputs
    get() = inputs.directoryInputs

val TransformInvocation.jarInputs
    get() = inputs.jarInputs
