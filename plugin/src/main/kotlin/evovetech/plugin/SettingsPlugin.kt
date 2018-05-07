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

package evovetech.plugin

import evovetech.plugin.extensions.AppExtension
import evovetech.plugin.extensions.SettingsAppExtension
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.kotlin.dsl.repositories
import java.io.File

class SettingsPlugin : BasePlugin<Settings>(SettingsAppExtension::class) {
    override
    fun Settings.initialize() {
        val settings = this
        val e = setParent(null)
        gradle.extras?.properties?.apply(e::loadProperties)
        settings.extras?.properties?.apply(e::loadProperties)
        configureEvaluated(gradle::settingsEvaluated)
        gradle.projectsLoaded {
            settings.configureRootProject(rootProject)
        }
        configureProject(gradle::beforeProject)
    }

    private
    fun configureEvaluated(
        afterEvaluated: (action: Action<in Settings>) -> Unit
    ) = afterEvaluated(Action {
        pluginManagement(evove.plugins::configure)
    })
}

fun Settings.configureRootProject(
    rootProject: Project
): Unit = rootProject.run {
    val plugin = plugins.apply(RootProjectPlugin::class.java)
    plugin.setParent(settings.evove).apply {
        buildscript.repositories(repos::closure)
        settings.setConfigDir().let {
            configDir = it.toFile()
        }
        initialize()
    }
}

fun Settings.configureProject(
    project: (action: Action<in Project>) -> Unit
) = project(Action {
    if (this !== rootProject) {
        val plugin = plugins.apply(ProjectPlugin::class.java)
        plugin.setParent(rootProject.evove).apply {
            initialize()
        }
    }
})

fun AppExtension<Project>.initialize() {
    val parent = this.parent
    project.run {
        parent?.let {
            configDir = it.configDir
            repos = it.repos
        }
        configDir.let {
            println("$this setting configDir=$it")
            ext["configDir"] = it
        }
        afterEvaluate {
            repositories(evove.repos::closure)
        }
    }
}

fun <T> Action<T>.closure(value: T) {
    execute(value)
}

fun Settings.setConfigDir(): File {
    println("$this try parent gradle config dir")
    var configDir = gradle.tryGetConfigDir()?.toFile()
    if (configDir == null) {
        println("$this try normal config dir")
        configDir = tryGetConfigDir()?.toFile()
    }
    if (configDir == null) {
        println("$this default config dir")
        configDir = evove.configDir
    } else {
        evove.apply {
            this.configDir = configDir
        }
    }
    gradle.extras?.properties?.set("configDir", configDir)
    extras?.properties?.set("configDir", configDir)
    return configDir
}

fun Gradle.tryGetConfigDir(): Any? {
    parent?.let { gp ->
        println("gp=$gp")
        gp.tryGetConfigDir().let {
            return it
        }
    }
    val any: Any = this
    return any.tryGetConfigDir()
}

fun Any.tryGetConfigDir() = extras?.properties?.get("configDir")?.apply {
    println("returning configDir: $this")
}

fun Any.toFile(): File = when (this) {
    is File -> this
    is String -> File(this)
    else -> toString().toFile()
}
