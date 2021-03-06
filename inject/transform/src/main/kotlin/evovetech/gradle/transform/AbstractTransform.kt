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
    private val isLibrary: Boolean,
    private val incremental: () -> Boolean
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
        return incremental()
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
