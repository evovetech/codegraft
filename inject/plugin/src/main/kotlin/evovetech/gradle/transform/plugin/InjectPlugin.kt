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

import com.android.build.gradle.BasePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.plugin.KaptExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinAndroidPluginWrapper

class InjectPlugin : Plugin<Project> {
    override
    fun apply(project: Project) {
        val wrapper = ProjectWrapper(project)

        project.plugins.withType(BasePlugin::class.java) {
            project.dependencies {
                add("implementation", "evovetech.codegraft:inject-annotations:$Version")
                add("implementation", "evovetech.codegraft:inject-core:$Version")
                add("implementation", "evovetech.codegraft:inject-android:$Version")
                add("runtimeOnly", "evovetech.codegraft:inject-runtime:$Version")
                add("kapt", "evovetech.codegraft:codegen-model:$Version")
            }

            wrapper.setup(extension)
        }

        project.plugins.withType(KotlinAndroidPluginWrapper::class.java) {
            project.extensions.findByType(KaptExtension::class.java)
                    ?.apply(wrapper::setup)
        }
    }

}
