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

import org.gradle.plugin.management.PluginManagementSpec
import java.util.TreeMap

open
class PluginManager(
    private val app: AppExtension<*>
) : TreeMap<String, PluginHandler>() {
    fun update(key: String, action: PluginHandler.() -> Unit) {
        val handler = getOrPut(key) {
            PluginHandler()
        }
        handler.action()
        println("$key=$handler")
    }

    internal
    fun configure(spec: PluginManagementSpec): Unit = spec.run {
        repositories {
            gradlePluginPortal()
        }
        repositories(app.repos)
        resolutionStrategy {
            eachPlugin {
                val reqId = requested.id.id
                app.plugins.entries
                        .find { (key, _) -> reqId.startsWith(key) }
                        ?.value
                        ?.addTo(this)
            }
        }
    }
}
