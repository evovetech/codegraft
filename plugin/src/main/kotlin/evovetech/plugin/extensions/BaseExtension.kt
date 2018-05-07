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
import evovetech.plugin.toFile
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.initialization.Settings
import java.io.File
import java.net.URI
import java.util.Properties

abstract
class BaseExtension<P : Any>(
    val project: P,
    val parent: BaseExtension<*>?
) {
    val app = Ext(project, "app")
    val pom = Ext(project, "pom")
    val ext = Ext(project, delegate = project.extras!!.properties)
    private val maps: Map<String, Ext<P>>
//    private val dynamo: Dynamo

    abstract
    val P.propertiesFile: File

    var configDir: File
        set(value) {
            ext["configDir"] = value
        }
        get() {
            ext["configDir"]?.toFile()?.let {
                return it
            }
            val p = project
            val root = when (p) {
                is Project -> p.rootDir
                is Settings -> p.settingsDir
                else -> parent?.configDir ?: throw IllegalStateException("can't find config dir")
            }
            val f = File(root, "gradle/config")
            return if (f.exists()) {
                f
            } else {
                File(root, "gradle")
            }.apply {
                configDir = this
            }
        }

    var repos: Action<in RepositoryHandler>
        set(value) {
            ext["repos"] = value
        }
        get() {
            ext["repos"]?.apply {
                when (this) {
                    is Action<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        return this as Action<in RepositoryHandler>
                    }
                }
            }
            return Action<RepositoryHandler> {
                mavenLocal()
                google()
                jcenter()
                maven {
                    url = URI.create("https://maven.fabric.io/public")
                }
            }.apply {
                repos = this
            }
        }

    var sourcererVersion: String?
        set(value) {
            value?.let {
                ext["sourcererVersion"] = it
                app["sourcererVersion"] = it
            }
        }
        get() = (ext["sourcererVersion"]
                 ?: app["sourcererVersion"]
                 ?: pom.properties["version"])
                ?.let {
                    val str = it.toString()
                    sourcererVersion = str
                    str
                }

    init {
        maps = LinkedHashMap<String, Ext<P>>().apply {
            this["app."] = app
            this["pom."] = pom
            this[""] = ext
        }
        project.apply {
            parent?.also {
                loadProperties(it)
                repos = it.repos
                configDir = it.configDir
                sourcererVersion = if (it.app.hasProperty("sourcererVersion")) {
                    it.app["sourcererVersion"].toString()
                } else {
                    it.pom.properties["version"].toString()
                }
            }
            extras?.apply {
                set("app", app)
                set("pom", pom)
                set("configDir", configDir)
            }
        }
    }

//    override
//    fun getAsDynamicObject() = dynamo

    protected
    fun P.loadProperties() = propertiesFile.let { file ->
        try {
            val props = Properties()
            println("loading properties: $file")
            props.load(file.reader())
            loadProperties(props)
        } catch (e: Throwable) {
            println("error loading properties file: $file")
//        e.printStackTrace(System.err)
        }
    }

    fun loadProperties(map: Map<*, *>) {
        map.entries.forEach { entry ->
            val entryKey = entry.key.toString()
            maps.entries.find {
                val prefix = it.key
                val ext = it.value
                if (entryKey.startsWith(prefix)) {
                    val key = entryKey.substring(prefix.length)
                    ext[key] = entry.value
                    true
                } else {
                    false
                }
            }
        }
    }

    private
    fun loadProperties(parent: BaseExtension<*>) {
        parent.project.extras?.properties?.apply(this::loadProperties)
        parent.ext.properties.map(ext::put)
        parent.app.properties.map(app::put)
        parent.pom.properties.map(pom::put)
    }
}
