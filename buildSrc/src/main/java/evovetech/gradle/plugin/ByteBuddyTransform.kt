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

package evovetech.gradle.plugin

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.Format.DIRECTORY
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.QualifiedContent.ContentType
import com.android.build.api.transform.QualifiedContent.DefaultContentType.CLASSES
import com.android.build.api.transform.QualifiedContent.DefaultContentType.RESOURCES
import com.android.build.api.transform.QualifiedContent.Scope
import com.android.build.api.transform.QualifiedContent.Scope.EXTERNAL_LIBRARIES
import com.android.build.api.transform.QualifiedContent.Scope.PROJECT
import com.android.build.api.transform.QualifiedContent.Scope.SUB_PROJECTS
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.utils.FileUtils
import java.io.File

class ByteBuddyTransform : Transform() {
    override
    fun getName(): String {
        return "ByteBuddyTransform"
    }

    override
    fun getOutputTypes(): MutableSet<ContentType> {
        return super.getOutputTypes()
    }

    override
    fun getInputTypes(): MutableSet<ContentType> {
        return mutableSetOf(
            CLASSES,
            RESOURCES
        )
    }

    override
    fun isIncremental(): Boolean {
        return false
    }

    override
    fun getScopes(): MutableSet<in Scope> {
        return mutableSetOf(
            PROJECT,
            SUB_PROJECTS,
            EXTERNAL_LIBRARIES
        )
    }

    override
    fun transform(transformInvocation: TransformInvocation) {
        transformInvocation.run()
    }

    private
    fun TransformInvocation.runner(format: Format): (content: QualifiedContent) -> Unit = {
        val outFile = outputProvider.getContentLocation(it.name, it.contentTypes, it.scopes, format)
        FileTransform(it, outFile, format).run()
    }

    fun TransformInvocation.run() {
        directoryInputs.forEach(runner(Format.DIRECTORY))
        jarInputs.forEach(runner(Format.JAR))
    }
}

val TransformInvocation.directoryInputs
    get() = inputs.flatMap { it.directoryInputs }

fun DirectoryInput.files() = file.listFiles()
        .toList()

val TransformInvocation.jarInputs
    get() = inputs.flatMap { it.jarInputs }

class FileTransform(
    val content: QualifiedContent,
    val out: File,
    val format: Format = DIRECTORY
) {
    init {
        if (format == DIRECTORY) {
            out.apply {
                if (exists()) {
                    FileUtils.deleteDirectoryContents(this)
                }
                mkdirs()
            }
        } else {
            FileUtils.deleteIfExists(out)
        }
        println("src=${content.file}, dest=$out")
    }

    fun run() {
        if (format == DIRECTORY) {
            content.file.listFiles()
                    .forEach(this::write)
        } else {
            FileUtils.copyFile(content.file, out)
        }
    }

    private
    fun dest(src: File, dir: File? = null): File {
        val dest = File(dir ?: out, src.name)
//        println("src=$src, dest=$dest")
        return dest
    }

    private
    fun File.writeFile(dest: File) {
        FileUtils.copyFile(this, dest)
    }

    private
    fun File.writeDir(dest: File) {
        dest.mkdirs()
        listFiles().forEach {
            it.write(dest)
        }
    }

    private
    fun File.write(dir: File? = null) {
        val dest = dest(this, dir)
        if (isDirectory) {
            writeDir(dest)
        } else {
            writeFile(dest)
        }
    }

    private
    fun write(file: File) {
        file.write()
    }
}
