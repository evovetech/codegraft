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

package evovetech.plugin.extensions

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.initialization.Settings
import java.io.File

/**
 * Created by layne on 3/7/18.
 */

abstract
class AppExtension<P : Any>(
    project: P,
    parent: BaseExtension<*>?
) : BaseExtension<P>(project, parent) {
    val plugins by lazy {
        PluginManager(this@AppExtension)
    }

    fun repos(action: Action<in RepositoryHandler>) {
        repos = action
    }

    fun plugins(action: Action<in PluginManager>) =
        action.execute(plugins)
}

open
class SettingsAppExtension(
    settings: Settings,
    parent: BaseExtension<*>?
) : AppExtension<Settings>(settings, parent) {
    override
    val Settings.propertiesFile by lazy {
        File(configDir, "defaultGradle.properties")
    }

    init {
        project.loadProperties()
    }
}

open
class ProjectAppExtension(
    project: Project,
    parent: BaseExtension<*>?
) : AppExtension<Project>(project, parent) {
    override
    val Project.propertiesFile: File by lazy {
        project.file("gradle.properties")
    }

    init {
        project.loadProperties()
    }
//
//    override
//    var parent: BaseExtension<*>?
//        get() = super.parent
//        set(value) {
//            super.parent = value
//        }
}
