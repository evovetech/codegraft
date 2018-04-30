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
        app.parent = null
        setConfigDir()
        configureEvaluated(gradle::settingsEvaluated)
        configureProject(gradle::beforeProject)
    }

    private
    fun configureEvaluated(
        afterEvaluated: (action: Action<in Settings>) -> Unit
    ) = afterEvaluated(Action {
        pluginManagement(app.plugins::configure)
//        pluginAware!!.run {
//            val dir: Any = gradle.extras?.properties?.get("configDir") ?: "gradle"
//            apply {
//                from("$dir/settings.gradle")
//            }
//        }
    })
}

fun <T> Action<T>.closure(value: T) {
    execute(value)
}

fun Settings.configureProject(
    project: (action: Action<in Project>) -> Unit
) = project(Action {
    gradle.extras?.properties?.forEach {
        project.extras?.set(it.key, it.value)
    }
    val settings = this@configureProject
    settings.extras?.properties?.forEach {
        project.extras?.set(it.key, it.value)
    }
    val app = if (this === rootProject) {
        val plugin = plugins.apply(RootProjectPlugin::class.java)
        plugin.app.apply {
            parent = settings.app
            buildscript.repositories(repos::closure)
        }
    } else {
        val plugin = plugins.apply(ProjectPlugin::class.java)
        plugin.app.apply {
            parent = rootProject.app
        }
    }
    app.apply {
        val p = parent
        p?.configDir?.let {
            configDir = it
        }
        println("$project setting configDir=$configDir")
        project.extras?.set("configDir", configDir)

        afterEvaluate {
            p?.let {
                repos = it.repos
            }
        }
    }
})

fun Settings.setConfigDir() {
    println("$this try parent gradle config dir")
    var configDir = gradle.tryGetConfigDir()?.toFile()
    if (configDir == null) {
        println("$this try normal config dir")
        configDir = tryGetConfigDir()?.toFile()
    }
    if (configDir == null) {
        println("$this default config dir")
        configDir = app.configDir
    } else {
        app.configDir = configDir
    }
    gradle.extras?.properties?.set("configDir", configDir)
    extras?.properties?.set("configDir", configDir)
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
