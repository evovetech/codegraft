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
import com.android.build.gradle.api.AnnotationProcessorOptions
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.BaseVariantOutput
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
import org.jetbrains.kotlin.daemon.common.findWithTransform
import org.jetbrains.kotlin.gradle.plugin.KaptExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinAndroidPluginWrapper
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.File
import java.util.EnumMap
import javax.xml.parsers.SAXParserFactory

private
const val sourcererVersion = "0.5.2"

class InjectPlugin : Plugin<Project> {
    override
    fun apply(project: Project) {
        val wrapper = ProjectWrapper(project)

        project.plugins.withType(BasePlugin::class.java) {
            project.dependencies {
                add("implementation", "evovetech.sourcerer:annotations:${sourcererVersion}")
                add("implementation", "evovetech.sourcerer:inject:${sourcererVersion}")
                add("implementation", "evovetech.sourcerer:inject-android:${sourcererVersion}")
                add("runtimeOnly", "evovetech.android.inject:core:$sourcererVersion")
                add("kapt", "evovetech.sourcerer:model:$sourcererVersion")
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
                    v.initTask()
                }
                is BaseVariant -> {
                    v.initTask()
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
            project.dependencies {
                add("kapt", "evovetech.sourcerer:lib-processor:$sourcererVersion")
                add("kaptAndroidTest", "evovetech.sourcerer:app-processor:$sourcererVersion")
                add("kaptTest", "evovetech.sourcerer:app-processor:$sourcererVersion")
            }
            android.setup()
        }
        else -> Unit
    }

    private
    fun Configuration.add(depName: String) {
        project.dependencies.add(name, "evovetech.sourcerer:$depName-processor:$sourcererVersion")
    }

    private
    fun AppExtension.setup() {
        applicationVariants.all {
            kapt { add("app") }
        }
        packagingOptions {
            exclude("**/*.srcr")
        }
    }

    private
    fun LibraryExtension.setup() {
        libraryVariants.setup {
            testVariants.setup()
            unitTestVariants.setup()
        }
    }

    private
    fun FeatureExtension.setup() {
        featureVariants.setup {
            testVariants.setup()
            unitTestVariants.setup()
        }
    }

    private
    fun <T : BaseVariant> T.kapt(config: Configuration.() -> Unit) {
        val kaptName = "kapt${name.capitalize()}"
        project.configurations
                .matching {
                    val matches = it.name == kaptName
                    println("$kaptName == ${it.name} ? $matches")
                    matches
                }
                .all(config)
    }

    private
    fun <T : BaseVariant> DomainObjectSet<T>.setup(
        block: T.() -> Unit = {}
    ) = all {
        val variant = this
        val adder: Configuration.() -> Unit = {
            when (variant) {
                is TestVariant -> add("app")
                else -> add("lib")
            }
        }
        annotationProcessorConfiguration.apply(adder)
        kapt(adder)
        block()
    }

    private
    fun <T : BaseVariant> T.initTask(
        options: AnnotationProcessorOptions = javaCompileOptions.annotationProcessorOptions
    ) = initTasks().findWithTransform { func ->
        val task = func(options)
        Pair(task != null, task)
    }

    private
    fun UnitTestVariant.initTask(): ManifestProcessorTask? {
        val options = javaCompileOptions.annotationProcessorOptions
        val tested = testedVariant
        return initTask(options) ?: when (tested) {
            is BaseVariant -> tested.initTask(options)
            else -> null
        }
    }

    private
    fun <T : BaseVariant> T.initTasks(): List<(AnnotationProcessorOptions) -> ManifestProcessorTask?> {
        val tasks = outputs.mapNotNull(::initialize)
        return tasks + when (this) {
            is TestVariant -> testedVariant.initTasks()
            is UnitTestVariant -> {
                val tested = testedVariant
                when (tested) {
                    is BaseVariant -> tested.initTasks()
                    else -> emptyList()
                }
            }
            else -> emptyList()
        }
    }
}

val ManifestProcessorTask.manifestFile
    get() = FileUtils.join(manifestOutputDirectory, SdkConstants.ANDROID_MANIFEST_XML)
            .toManifestFile()

fun initialize(
    output: BaseVariantOutput
): (AnnotationProcessorOptions) -> ManifestProcessorTask? = initialize(output.processManifest)

fun initialize(
    task: ManifestProcessorTask
) = { options: AnnotationProcessorOptions ->
    task.manifestFile?.run {
        options.apply {
            (Package.value ?: InstTargetPkg.value)?.let { pkg ->
                arguments["evovetech.processor.package"] = pkg
            }
            ApplicationName.value?.let {
                arguments["evovetech.processor.application"] = it
            }

            println("\nprocessor arguments {")
            arguments.forEach { (k, v) ->
                println("  $k=$v")
            }
            println("}\n")
        }
        task
    }
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

