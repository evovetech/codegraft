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

package evovetech.gradle.transform.plugin

import com.android.build.api.transform.Transform
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.BasePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

abstract
class TransformPlugin : Plugin<Project> {
    abstract
    fun BaseExtension.transformer(
        project: Project
    ): Transform?

    final override
    fun apply(project: Project) {
        project.plugins.withType(BasePlugin::class.java) {
            println("plugin=$this")
            extension.setup(project)
        }
    }

    private
    fun BaseExtension.setup(project: Project) {
        println("android ext = $this")
        transformer(project)?.let {
            registerTransform(it)
        }
    }
}
