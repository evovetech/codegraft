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

import com.android.build.api.transform.QualifiedContent.ContentType
import com.android.build.api.transform.QualifiedContent.DefaultContentType.CLASSES
import com.android.build.api.transform.QualifiedContent.Scope
import com.android.build.api.transform.QualifiedContent.Scope.EXTERNAL_LIBRARIES
import com.android.build.api.transform.QualifiedContent.Scope.PROJECT
import com.android.build.api.transform.QualifiedContent.Scope.SUB_PROJECTS
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation

abstract
class AbstractTransform(
    private val name: String,
    private val isLibrary: Boolean
) : Transform() {

    override
    fun getName() = name

    override
    fun getInputTypes(): Set<ContentType> {
        return setOf(
            CLASSES
        )
    }

    override
    fun getOutputTypes(): Set<ContentType> {
        return inputTypes
    }

    override
    fun isIncremental(): Boolean {
        return true
    }

    override
    fun getScopes(): MutableSet<in Scope> =
        mutableSetOf(PROJECT).apply {
            if (!isLibrary) {
                add(SUB_PROJECTS)
                add(EXTERNAL_LIBRARIES)
            }
        }

    override
    fun getReferencedScopes(): MutableSet<in Scope> = mutableSetOf(
        PROJECT,
        SUB_PROJECTS,
        EXTERNAL_LIBRARIES
    )

    abstract
    fun TransformInvocation.runRun(): RunRun

    override
    fun transform(transformInvocation: TransformInvocation) = transformInvocation.run {
        runRun()
    }.run()
}
