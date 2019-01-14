/*
 * Copyright (C) 2018 evove.tech
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package evovetech.gradle.transform.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.FeatureExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.LibraryVariant
import com.android.build.gradle.api.TestVariant
import com.android.build.gradle.api.UnitTestVariant
import evovetech.gradle.transform.InjectRunRunTransform
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.plugin.KaptAnnotationProcessorOptions
import org.jetbrains.kotlin.gradle.plugin.KaptExtension

class ProjectWrapper(
    project: Project,
    private val extension: InjectExtension
) : Project by project {

    fun setupKapt(obj: Any) {
        val kapt = obj as? KaptExtension
                   ?: return
        kapt.arguments {
            val v = variant
            when (v) {
                is UnitTestVariant -> {
                    v.initUnitTestVariantTask(this)
                }
                is BaseVariant -> {
                    v.initBaseVariantTask(this)
                }
            }
        }
    }

    fun setupAndroid(obj: Any) {
        val android = obj as? BaseExtension
                      ?: return

        project.dependencies {
            add("implementation", "evovetech.codegraft:inject-annotations:$Version")
            add("implementation", "evovetech.codegraft:inject-core:$Version")
            add("implementation", "evovetech.codegraft:inject-android:$Version")
            add("runtimeOnly", "evovetech.codegraft:inject-runtime:$Version")
            add("kapt", "evovetech.codegraft:codegen-model:$Version")
        }

        addTransform(android)
        addDependencies(android)
    }

    private
    fun addTransform(android: BaseExtension) {
        val isLibrary = when (android) {
            is LibraryExtension -> true
            else -> false
        }
        val transform = InjectRunRunTransform(
            isLibrary = isLibrary,
            incremental = extension::incremental,
            bootClasspath = android::getBootClasspath
        )
        android.registerTransform(transform)
    }

    private
    fun addDependencies(android: BaseExtension): Unit = when (android) {
        is AppExtension -> {
            android.setup()
        }
        is FeatureExtension -> {
            android.setup()
        }
        is LibraryExtension -> {
            android.setup()
        }
        else -> Unit
    }

    private
    fun Configuration.add(depName: String) {
        project.dependencies.add(name, "evovetech.codegraft:codegen-$depName-processor:$Version")
    }

    private
    fun AppExtension.setup() {
        applicationVariants.apkSetup()
        packagingOptions {
            exclude("**/*.srcr")
        }
    }

    private
    fun LibraryExtension.setup() {
        libraryVariants.libSetup()
        packagingOptions {
            merge("**/*.srcr")
        }
    }

    private
    fun FeatureExtension.setup() {
        libraryVariants.libSetup()
        featureVariants.apkSetup()
        packagingOptions {
            exclude("**/*.srcr")
        }
    }

    private
    fun DomainObjectSet<out ApkVariant>.apkSetup() = all {
        this.setup()
    }

    private
    fun DomainObjectSet<out LibraryVariant>.libSetup() = all {
        this.setup()
        testVariant?.setup()
        unitTestVariant?.setup()
    }

    private
    fun BaseVariant.setup() {
        val variant = this
        val adder: Configuration.() -> Unit = {
            when (variant) {
                is TestVariant,
                is UnitTestVariant,
                is ApkVariant -> add("app")
                is LibraryVariant -> add("lib")
            }
        }
        annotationProcessorConfiguration.apply(adder)
        kapt(adder)
    }

    private
    fun <T : BaseVariant> T.kapt(config: Configuration.() -> Unit) {
        val suffix = when {
            name.contains("UnitTest") -> {
                val index = name.indexOf("UnitTest")
                "test${name.substring(0, index).capitalize()}"
            }
            name.contains("AndroidTest") -> {
                val index = name.indexOf("AndroidTest")
                "androidTest${name.substring(0, index).capitalize()}"
            }
            else -> name
        }
        val kaptName = "kapt${suffix.capitalize()}"
        project.configurations
                .matching { it.name == kaptName }
                .all(config)
    }

    private
    fun <T : BaseVariant> T.initBaseVariantTask(
        options: KaptAnnotationProcessorOptions
    ): ManifestFile? {
        val buildConfig = generateBuildConfig
        buildConfig?.apply {
            val arguments = mutableMapOf<String, Any>().apply {
                this["evovetech.processor.package"] = buildConfig.appPackageName
            }
            arguments.forEach { (k, v) ->
                options.arg(k, v)
            }
        }

        val manifestTask = outputs
                .mapNotNull { it.processManifest?.manifestFile }
                .firstOrNull()
        manifestTask?.initialize(options)
        return manifestTask
    }

    private
    fun UnitTestVariant.initUnitTestVariantTask(
        options: KaptAnnotationProcessorOptions
    ): ManifestFile? {
        val tested = testedVariant
        return initBaseVariantTask(options) ?: when (tested) {
            is BaseVariant -> tested.initBaseVariantTask(options)
            else -> null
        }
    }
}
