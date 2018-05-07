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
import evovetech.plugin.extensions.BaseExtension
import evovetech.plugin.extensions.ProjectAppExtension
import evovetech.plugin.extensions.SettingsAppExtension
import evovetech.plugin.util.getValue
import evovetech.plugin.util.once
import evovetech.plugin.util.setValue
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.plugins.PluginAware
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.get
import kotlin.reflect.KClass

abstract
class BasePlugin<T : Any>(
    private val extType: KClass<out AppExtension<T>>
) : Plugin<T> {
    var project by once<T> {
        throw IllegalStateException("project not set")
    }
    var evove by once {
        project.createEvove()
    }

    abstract
    fun T.initialize()

    final override
    fun apply(target: T) = target.run {
        project = target
        println("${this@BasePlugin::class.simpleName} { target=$this }")
        target.initialize()
    }

    fun setParent(
        parent: BaseExtension<*>?
    ): AppExtension<T> = project.createEvove(parent)
            .apply { evove = this }

    private
    fun T.createEvove(
        parent: BaseExtension<*>? = null
    ): AppExtension<T> = extensions!!
            .create("evove", extType.java, this, parent)
}

/*

def loadDefaults() {
def configDir = setConfigDir(settings)
return [
        app: loadDefault(configDir, 'app'),
        pom: loadDefault(configDir, 'pom')
]
}

def loadDefault(configDir, String ext) {
def expando = new Expando()
loadProperties(this, "${configDir}/defaultGradle.properties", expando, "${ext}.")
this.ext.set(ext, expando)
return expando
}

static def setConfigDir(settings) {
def gradle = settings.gradle
println "try parent gradle config dir"
def configDir = tryGetGradleConfigDir(gradle)
if (configDir == null) {
    println "try normal config dir"
    configDir = tryGetConfigDir(settings.ext)
}
if (configDir == null) {
    println "default config dir"
    configDir = "${settings.settingsDir}/gradle"
}
settings.gradle.ext.configDir = configDir
settings.ext.configDir = configDir
return configDir
}

static def tryGetGradleConfigDir(gradle) {
def gp = gradle.parent
if (gp != null) {
    println "gp = ${gp}"
    def configDir = tryGetGradleConfigDir(gp)
    if (configDir != null) {
        return configDir
    }
}
return tryGetConfigDir(gradle.ext)
}

static def tryGetConfigDir(ext) {
if (ext.has('configDir')) {
    println "returning configDir: ${ext.configDir}"
    return ext.configDir
}
return null
}

static def loadProject(p) {
File propsFile = p.file('gradle.properties')
def app = new Expando(p.app.properties)
p.ext.app = app
loadProperties(p, propsFile, app, 'app.')
def pom = new Expando(p.pom.properties)
p.ext.pom = pom
loadProperties(p, propsFile, pom, 'pom.')
}

static def loadProperties(p, fileName, obj, objPrefix) {
def file = p.file(fileName)
if (!file.exists()) {
    return
}
def props = new Properties()
props.load(file.newReader())
props.each { prop ->
    def key = prop.key as String
    if (key.startsWith(objPrefix)) {
        key = key.substring(objPrefix.length())
        obj."${key}" = prop.value
    }
}
}

 */

val Settings.evove: SettingsAppExtension
    get() {
        val evove = extensions?.get("evove")
        return when (evove) {
            is SettingsAppExtension -> evove
            else -> throw IllegalStateException("app not set for $this")
        }
    }

val Project.evove: ProjectAppExtension
    get() {
        val evove = extensions["evove"]
        return when (evove) {
            is ProjectAppExtension -> evove
            else -> throw IllegalStateException("app not set for $this")
        }
    }

val <T> T.extensionAware: ExtensionAware?
    get() = when (this) {
        is ExtensionAware -> this
        else -> null
    }

val <T> T.extras: ExtraPropertiesExtension?
    get() = extensionAware?.extra

val <T> T.extensions: ExtensionContainer?
    get() = extensionAware?.extensions

val <T> T.pluginAware: PluginAware?
    get() = when (this) {
        is PluginAware -> this
        else -> null
    }
