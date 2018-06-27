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
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.tasks.ManifestProcessorTask
import com.android.manifmerger.PlaceholderHandler
import com.android.utils.XmlUtils
import com.google.common.collect.Maps
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
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.File
import java.util.EnumMap
import javax.xml.parsers.SAXParserFactory

class InjectPlugin : Plugin<Project> {
    override
    fun apply(project: Project) {
        project.plugins.withType(BasePlugin::class.java) {
            mapPlugin(project)
                    .setup()
        }
    }
}

fun BasePlugin<*>.mapPlugin(project: Project) =
    PluginWrapper(project, this)

class PluginWrapper<P : BasePlugin<*>>(
    project: Project,
    private val plugin: P
) : Project by project {

    fun setup() {
        plugin.extension.setup()
    }

    private
    fun BaseExtension.setup(): Unit = when (this) {
        is AppExtension -> setup()
        is LibraryExtension -> setup()
        is FeatureExtension -> setup()
        else -> Unit
    }

    private
    fun AppExtension.setup() {
        applicationVariants.setup()
    }

    private
    fun LibraryExtension.setup() {
        libraryVariants.setup()
    }

    private
    fun FeatureExtension.setup() {
        featureVariants.setup()
    }

    private
    fun <T : BaseVariant> DomainObjectSet<T>.setup(block: T.() -> Unit = {}) = all {
        val packageName = generateBuildConfig.appPackageName
        javaCompileOptions.annotationProcessorOptions.apply {
            arguments["evovetech.processor.package"] = packageName
        }
        outputs.map(BaseVariantOutput::getProcessManifest)
                .map(this::initialize)
    }
}

fun BaseVariant.initialize(
    task: ManifestProcessorTask
) = task.doLast {
    val file = task.manifestOutputDirectory
                       ?.let(File::listFiles)
                       ?.find { it.extension == "xml" }
               ?: return@doLast
    file.toManifestFile().run {
        javaCompileOptions.annotationProcessorOptions.apply {
            ApplicationName.value?.let {
                arguments["evovetech.processor.manifest"] = it
            }
            println("\nprocessor arguments {")
            arguments.forEach { (k, v) ->
                println("  $k=$v")
            }
            println("}\n")
        }
    }
}

private
val ParserFactory: SAXParserFactory by lazy {
    val factory = SAXParserFactory.newInstance()
    XmlUtils.configureSaxFactory(factory, true, false)
    factory
}

fun File.toManifestFile(): ManifestFile =
    ManifestFile(this)

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

