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

package evovetech.gradle.transform.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

class InjectPlugin
@Inject constructor(
    private val objectFactory: ObjectFactory
) : Plugin<Project> {
    override
    fun apply(project: Project) {
        val extension = project.extensions.create(
            "codegraft",
            InjectExtension::class.java,
            project.logger
        )
        val wrapper = ProjectWrapper(project, extension)
        project.extensions.findByName("android")
                ?.let(wrapper::setupAndroid)
        project.extensions.findByName("kapt")
                ?.let(wrapper::setupKapt)
    }
}

