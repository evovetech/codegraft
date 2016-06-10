/*
 * Copyright 2016 Layne Mobile, LLC
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

package sourcerer

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException


class SourcererPlugin implements Plugin<Project> {
    @Override
    void apply(Project target) {
        if (!project.plugins.findPlugin("com.android.application") && !project.plugins.findPlugin("android")) {
            throw new ProjectConfigurationException("The android plugin must be applied to the project", null)
        }
        project.apply(plugin: 'com.neenbedankt.android-apt')
        project.android.applicationVariants.all { variant ->
            String taskName = "generate${variant.name.capitalize()}StashExtensionSources"
            SourcesTask task = project.tasks.newReader(taskName, SourcesTask)
            task.inputFiles = new ArrayList<>(variant.getCompileLibraries())
            task.outputDir = new File(project.buildDir, "generated/source/stash/${variant.dirName}")
            task.init()
            variant.registerJavaGeneratingTask(task, task.outputDir)
        }
        project.android.packagingOptions {
            for (ExtensionType kind : ExtensionType.values()) {
                exclude kind.resourceFilePath()
            }
        }
        project.dependencies {
//            apt "com.laynemobile.stash:stash-compiler:${Version.NAME}"
        }
    }
}
