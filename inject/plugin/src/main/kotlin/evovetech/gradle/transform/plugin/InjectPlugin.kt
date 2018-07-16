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

import com.android.SdkConstants
import com.android.SdkConstants.ATTR_FUNCTIONAL_TEST
import com.android.SdkConstants.ATTR_HANDLE_PROFILING
import com.android.SdkConstants.ATTR_LABEL
import com.android.SdkConstants.ATTR_MIN_SDK_VERSION
import com.android.SdkConstants.ATTR_NAME
import com.android.SdkConstants.ATTR_PACKAGE
import com.android.SdkConstants.ATTR_SPLIT
import com.android.SdkConstants.ATTR_TARGET_PACKAGE
import com.android.SdkConstants.ATTR_TARGET_SDK_VERSION
import com.android.SdkConstants.ATTR_VERSION_CODE
import com.android.SdkConstants.ATTR_VERSION_NAME
import com.android.SdkConstants.NS_RESOURCES
import com.android.SdkConstants.TAG_APPLICATION
import com.android.SdkConstants.TAG_INSTRUMENTATION
import com.android.SdkConstants.TAG_MANIFEST
import com.android.SdkConstants.TAG_USES_SDK
import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.BasePlugin
import com.android.build.gradle.FeatureExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.LibraryVariant
import com.android.build.gradle.api.TestVariant
import com.android.build.gradle.api.UnitTestVariant
import com.android.build.gradle.tasks.ManifestProcessorTask
import com.android.manifmerger.PlaceholderHandler
import com.android.utils.FileUtils
import com.android.utils.XmlUtils
import com.google.common.collect.Maps
import evovetech.gradle.transform.InjectRunRunTransform
import evovetech.gradle.transform.plugin.ManifestFile.Attribute.ApplicationName
import evovetech.gradle.transform.plugin.ManifestFile.Attribute.InstFunctionalTest
import evovetech.gradle.transform.plugin.ManifestFile.Attribute.InstHandleProp
import evovetech.gradle.transform.plugin.ManifestFile.Attribute.InstLabel
import evovetech.gradle.transform.plugin.ManifestFile.Attribute.InstName
import evovetech.gradle.transform.plugin.ManifestFile.Attribute.InstTargetPkg
import evovetech.gradle.transform.plugin.ManifestFile.Attribute.MinSdkVersion
import evovetech.gradle.transform.plugin.ManifestFile.Attribute.Package
import evovetech.gradle.transform.plugin.ManifestFile.Attribute.Split
import evovetech.gradle.transform.plugin.ManifestFile.Attribute.TargetSdkVersion
import evovetech.gradle.transform.plugin.ManifestFile.Attribute.VersionCode
import evovetech.gradle.transform.plugin.ManifestFile.Attribute.VersionName
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.plugin.KaptAnnotationProcessorOptions
import org.jetbrains.kotlin.gradle.plugin.KaptExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinAndroidPluginWrapper
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.File
import java.util.EnumMap
import javax.xml.parsers.SAXParserFactory

class InjectPlugin : Plugin<Project> {
    override
    fun apply(project: Project) {
        val wrapper = ProjectWrapper(project)

        project.plugins.withType(BasePlugin::class.java) {
            project.dependencies {
                add("implementation", "evovetech.sourcerer:annotations:$Version")
                add("implementation", "evovetech.sourcerer:inject:$Version")
                add("implementation", "evovetech.sourcerer:inject-android:$Version")
                add("runtimeOnly", "evovetech.android.inject:core:$Version")
                add("kapt", "evovetech.sourcerer:model:$Version")
            }

            wrapper.setup(extension)
        }

        project.plugins.withType(KotlinAndroidPluginWrapper::class.java) {
            project.extensions.findByType(KaptExtension::class.java)
                    ?.apply(wrapper::setup)
        }
    }

}

class ProjectWrapper(
    project: Project
) : Project by project {
    fun setup(kapt: KaptExtension) {
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

    fun setup(android: BaseExtension) {
        addTransform(android)
        addDependencies(android)
    }

    private
    fun addTransform(android: BaseExtension) {
        val isLibrary = when (android) {
            is LibraryExtension -> true
            else -> false
        }
        val transform = InjectRunRunTransform(isLibrary, android::getBootClasspath)
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
        project.dependencies.add(name, "evovetech.sourcerer:$depName-processor:$Version")
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
                .matching {
                    val matches = it.name == kaptName
                    println("$kaptName == ${it.name} ? $matches")
                    matches
                }
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
            println("\nbuildConfig processor arguments {")
            arguments.forEach { (k, v) ->
                println("  $k=$v")
                options.arg(k, v)
            }
            println("}\n")
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

val ManifestProcessorTask.manifestFile
    get() = FileUtils.join(manifestOutputDirectory, SdkConstants.ANDROID_MANIFEST_XML)
            .toManifestFile()

fun ManifestFile.initialize(
    options: KaptAnnotationProcessorOptions
) {
    val arguments = mutableMapOf<String, Any>().apply {
        (Package.value ?: InstTargetPkg.value)?.let { pkg ->
            this["evovetech.processor.package"] = pkg
        }
        ApplicationName.value?.let {
            this["evovetech.processor.application"] = it
        }
    }
    println("\nprocessor arguments {")
    arguments.forEach { (k, v) ->
        println("  $k=$v")
        options.arg(k, v)
    }
    println("}\n")
}

private
val ParserFactory: SAXParserFactory by lazy {
    val factory = SAXParserFactory.newInstance()
    XmlUtils.configureSaxFactory(factory, true, false)
    factory
}

fun File.toManifestFile(): ManifestFile? = if (exists()) {
    ManifestFile(this)
} else {
    null
}

class ManifestFile(
    private val manifestFile: File
) {
    private
    val attrs by lazy {
        try {
            val handler = Handler()
            val saxParser = XmlUtils.createSaxParser(ParserFactory)
            saxParser.parse(manifestFile, handler)
            handler.attributes
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    val Attribute.value: String?
        get() = attrs[this]

    enum
    class Attribute {
        ApplicationName,
        Split,
        Package,
        VersionCode,
        VersionName,
        InstLabel,
        InstFunctionalTest,
        InstName,
        InstHandleProp,
        InstTargetPkg,
        MinSdkVersion,
        TargetSdkVersion;
    }

    class Handler : DefaultHandler() {
        private
        val attrs = Maps.newEnumMap<Attribute, String>(Attribute::class.java)!!
        val attributes: EnumMap<Attribute, String> by lazy {
            EnumMap(attrs).apply {
                println("attrs=$this")
            }
        }

        override
        fun startElement(
            uri: String?,
            localName: String?,
            qName: String?,
            attributes: Attributes
        ) {
            if (!uri.isNullOrEmpty()) {
                return
            }

            fun getValue(key: String): String? =
                attributes.getValue(NS_RESOURCES, key)

            when (localName) {
                TAG_MANIFEST -> {
                    Split.put(attributes.getValue("", ATTR_SPLIT))
                    Package.put(attributes.getValue("", ATTR_PACKAGE))
                    VersionCode.put(getValue(ATTR_VERSION_CODE))
                    VersionName.put(getValue(ATTR_VERSION_NAME))
                }
                TAG_INSTRUMENTATION -> {
                    InstLabel.put(getValue(ATTR_LABEL))
                    InstFunctionalTest.put(getValue(ATTR_FUNCTIONAL_TEST))
                    InstName.put(getValue(ATTR_NAME))
                    InstHandleProp.put(getValue(ATTR_HANDLE_PROFILING))
                    InstTargetPkg.put(getValue(ATTR_TARGET_PACKAGE))
                }
                TAG_USES_SDK -> {
                    MinSdkVersion.put(getValue(ATTR_MIN_SDK_VERSION))
                    TargetSdkVersion.put(getValue(ATTR_TARGET_SDK_VERSION))
                }
                TAG_APPLICATION -> {
                    ApplicationName.put(getValue(ATTR_NAME))
                }
            }
        }

        fun Attribute.put(value: String?) {
            putValue(this, value)
        }

        private
        fun putValue(
            attribute: Attribute,
            value: String?
        ) {
            if (value != null && !PlaceholderHandler.isPlaceHolder(value)) {
                attrs[attribute] = value
            }
        }
    }
}

