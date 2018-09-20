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

import com.android.SdkConstants
import com.android.build.gradle.tasks.ManifestProcessorTask
import com.android.manifmerger.PlaceholderHandler
import com.android.utils.FileUtils
import com.android.utils.XmlUtils
import com.google.common.collect.Maps
import evovetech.gradle.transform.plugin.ManifestFile.Attribute
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
import org.jetbrains.kotlin.gradle.plugin.KaptAnnotationProcessorOptions
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.File
import java.util.EnumMap
import javax.xml.parsers.SAXParserFactory

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
            EnumMap(attrs)
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
                attributes.getValue(SdkConstants.NS_RESOURCES, key)

            when (localName) {
                SdkConstants.TAG_MANIFEST -> {
                    Split.put(attributes.getValue("", SdkConstants.ATTR_SPLIT))
                    Package.put(attributes.getValue("", SdkConstants.ATTR_PACKAGE))
                    VersionCode.put(getValue(SdkConstants.ATTR_VERSION_CODE))
                    VersionName.put(getValue(SdkConstants.ATTR_VERSION_NAME))
                }
                SdkConstants.TAG_INSTRUMENTATION -> {
                    InstLabel.put(getValue(SdkConstants.ATTR_LABEL))
                    InstFunctionalTest.put(getValue(SdkConstants.ATTR_FUNCTIONAL_TEST))
                    InstName.put(getValue(SdkConstants.ATTR_NAME))
                    InstHandleProp.put(getValue(SdkConstants.ATTR_HANDLE_PROFILING))
                    InstTargetPkg.put(getValue(SdkConstants.ATTR_TARGET_PACKAGE))
                }
                SdkConstants.TAG_USES_SDK -> {
                    MinSdkVersion.put(getValue(SdkConstants.ATTR_MIN_SDK_VERSION))
                    TargetSdkVersion.put(getValue(SdkConstants.ATTR_TARGET_SDK_VERSION))
                }
                SdkConstants.TAG_APPLICATION -> {
                    ApplicationName.put(getValue(SdkConstants.ATTR_NAME))
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

val ManifestProcessorTask.manifestFile
    get() = FileUtils.join(manifestOutputDirectory, SdkConstants.ANDROID_MANIFEST_XML)
            .toManifestFile()

fun ManifestFile.initialize(
    options: KaptAnnotationProcessorOptions
) {
    val arguments = mutableMapOf<String, Any>().apply {
        (Attribute.Package.value ?: InstTargetPkg.value)?.let { pkg ->
            this["evovetech.processor.package"] = pkg
        }
        ApplicationName.value?.let {
            this["evovetech.processor.application"] = it
        }
    }
    arguments.forEach { (k, v) ->
        options.arg(k, v)
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

