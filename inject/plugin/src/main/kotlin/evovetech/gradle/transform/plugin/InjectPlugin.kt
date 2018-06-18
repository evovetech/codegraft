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

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.BasePlugin
import com.android.build.gradle.FeatureExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project

class InjectPlugin : Plugin<Project> {
    override
    fun apply(project: Project) {
        project.plugins.withType(BasePlugin::class.java) {
            extension.setup()
        }
    }

    private
    fun BaseExtension.setup() {
        val variants: DomainObjectSet<out BaseVariant> = when (this) {
            is AppExtension -> applicationVariants
            is LibraryExtension -> libraryVariants
            is FeatureExtension -> featureVariants
            else -> return
        }
        variants.all {
            val options = javaCompileOptions.annotationProcessorOptions
            val packageName = generateBuildConfig.appPackageName
            options.arguments["evovetech.processor.package"] = packageName
            println("\nprocessor arguments: ${options.arguments}\n")
        }
    }
}
