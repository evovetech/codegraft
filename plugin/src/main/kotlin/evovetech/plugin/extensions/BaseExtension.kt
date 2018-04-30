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

import evovetech.plugin.extras
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.initialization.Settings
import java.io.File
import java.net.URI
import java.util.Properties

abstract
class BaseExtension<P : Any>(
    val project: P
) : BaseExpando() {
    abstract
    val P.propertiesFile: File

    var configDir: File by property {
        val p = project
        val root = when (p) {
            is Project -> p.rootDir
            is Settings -> p.settingsDir
            else -> parent?.configDir ?: throw IllegalStateException("can't find config dir")
        }
        File(root, "gradle")
    }

    val pom: PomExtension by property {
        PomExtension()
    }

    var repos: Action<in RepositoryHandler> by property {
        Action<RepositoryHandler> {
            mavenLocal()
            google()
            jcenter()
            maven {
                url = URI.create("https://maven.fabric.io/public")
            }
        }
    }

//    init {
//        project.extras?.apply {
//            set("configDir", configDir)
//            set("pom", )
//        }
//        println("configDir=$configDir")
//        println("pom=$pom")
//        println("repos=$repos")
//    }

    open
    var parent: BaseExtension<*>? = null
        set(value) {
            val app = this
            val p = project
            value?.also {
                field = it
                app.loadProperties(it)
                app.pom.loadProperties(it.pom)
                repos = it.repos
                configDir = it.configDir
            }
            p.loadProperties()
        }

    private
    fun P.loadProperties() = try {
        val props = Properties()
        props.load(propertiesFile.reader())
        loadProperties(props)
    } catch (e: Throwable) {
//        e.printStackTrace(System.err)
    }

    private
    fun P.loadProperties(map: Map<*, *>) {
        val prefixes = LinkedHashMap<String, BaseExpando>()
        val app = this@BaseExtension
        prefixes["app."] = app
        prefixes["pom."] = app.pom
        map.entries.forEach { entry ->
            val entryKey = entry.key.toString()
            val found = prefixes.entries.find {
                val prefix = it.key
                val ext = it.value
                if (entryKey.startsWith(prefix)) {
                    val key = entryKey.substring(prefix.length)
                    ext.setProperty(key, entry.value)
                    true
                } else {
                    false
                }
            }
            if (found == null) {
                extras?.properties?.put(entryKey, entry.value)
            }
        }
    }
}

fun BaseExpando.loadProperties(parent: BaseExpando) {
    parent.properties.forEach { (key, value) ->
        setProperty(key.toString(), value)
    }
}
