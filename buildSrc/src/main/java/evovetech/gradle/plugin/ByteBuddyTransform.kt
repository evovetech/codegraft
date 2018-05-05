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

import com.android.build.api.transform.Format.DIRECTORY
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.QualifiedContent.ContentType
import com.android.build.api.transform.QualifiedContent.Scope
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.utils.FileUtils
import java.io.File

class ByteBuddyTransform : Transform() {
    override
    fun getName(): String {
        return "ByteBuddyTransform"
    }

    override fun getOutputTypes(): MutableSet<ContentType> {
        return super.getOutputTypes()
    }

    override
    fun getInputTypes(): MutableSet<ContentType> {
        return mutableSetOf(QualifiedContent.DefaultContentType.CLASSES)
    }

    override
    fun isIncremental(): Boolean {
        return false
    }

    override
    fun getScopes(): MutableSet<in Scope> {
        return mutableSetOf(QualifiedContent.Scope.PROJECT)
    }

    override
    fun transform(transformInvocation: TransformInvocation) {
        transformInvocation.run()
    }

    fun TransformInvocation.run() {
        val outDir = outputProvider.getContentLocation("evove", outputTypes, scopes, DIRECTORY)
        FileTransform(outDir).apply {
            inputs.flatMap { it.directoryInputs }
                    .flatMap { it.file.listFiles().toList() }
                    .forEach {
                        it.write()
                    }
        }
    }
}

class FileTransform(
    val outDir: File
) {
    init {
        if (outDir.exists()) {
            FileUtils.deleteDirectoryContents(outDir)
        }
        outDir.mkdirs()
    }

    fun dest(src: File, dir: File? = null): File {
        val dest = File(dir ?: outDir, src.name)
        println("src=$src, dest=$dest")
        return dest
    }

    fun File.writeFile(dest: File) {
        FileUtils.copyFile(this, dest)
    }

    fun File.writeDir(dest: File) {
        dest.mkdirs()
        listFiles().forEach {
            it.write(dest)
        }
    }

    fun File.write(dir: File? = null) {
        val dest = dest(this, dir)
        if (isDirectory) {
            writeDir(dest)
        } else {
            writeFile(dest)
        }
    }
}
