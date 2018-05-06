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
import com.android.build.api.transform.Format.JAR
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.QualifiedContent.ContentType
import com.android.build.api.transform.QualifiedContent.DefaultContentType.CLASSES
import com.android.build.api.transform.QualifiedContent.DefaultContentType.RESOURCES
import com.android.build.api.transform.QualifiedContent.Scope
import com.android.build.api.transform.QualifiedContent.Scope.EXTERNAL_LIBRARIES
import com.android.build.api.transform.QualifiedContent.Scope.PROJECT
import com.android.build.api.transform.QualifiedContent.Scope.SUB_PROJECTS
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.utils.FileUtils
import net.bytebuddy.ByteBuddy
import net.bytebuddy.ClassFileVersion
import net.bytebuddy.build.EntryPoint
import net.bytebuddy.dynamic.ClassFileLocator
import net.bytebuddy.dynamic.scaffold.inline.MethodNameTransformer
import net.bytebuddy.pool.TypePool
import java.io.File
import java.io.IOException
import java.util.jar.JarFile

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
        return true
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
    fun transform(transformInvocation: TransformInvocation) = Runner(transformInvocation)
            .run()
}

val Collection<TransformInput>.directoryInputs
    get() = flatMap { it.directoryInputs }

val Collection<TransformInput>.jarInputs
    get() = flatMap { it.jarInputs }

val TransformInvocation.directoryInputs
    get() = inputs.directoryInputs

val TransformInvocation.jarInputs
    get() = inputs.jarInputs

fun TransformInvocation.transformer(
    format: Format
): (content: QualifiedContent) -> RootTransform = {
    if (format == DIRECTORY) {
        RootDirTransform(this, it as DirectoryInput)
    } else {
        val outFile = outputProvider.getContentLocation(it.name, it.contentTypes, it.scopes, format)
        RootJarTransform(it as JarInput, outFile)
    }
}

class RootDirTransform(
    invocation: TransformInvocation,
    override val src: DirectoryInput
) : RootTransform() {
    override
    val dest: File by lazy {
        src.let {
            invocation.outputProvider.getContentLocation(it.name, it.contentTypes, it.scopes, format)
        }.apply {
            FileUtils.deleteRecursivelyIfExists(this)
            FileUtils.deleteIfExists(this)
            mkdirs()
        }
    }
    override
    val format = Format.DIRECTORY
    val children by lazy {
        src.file.listFiles().map { src ->
            val dest = File(src.name)
            if (src.isDirectory) {
                DirTransform(this, src, dest)
            } else {
                FileTransform(this, src, dest)
            }
        }
    }
    val dirs by lazy {
        children.filterIsInstance<DirTransform>()
    }
    val files by lazy {
        children.filterIsInstance<FileTransform>()
    }
    override
    val all by lazy {
        files + dirs.flatMap {
            it.dirs + it
        }.flatMap {
            it.files
        }
    }

    override
    fun write(child: BaseTransform) {
        val src = child.src
        val dest = File(this.dest, child.dest.path)
        dest.parentFile.mkdirs()
//        println("$src -> $dest")
        FileUtils.copyFile(src, dest)
    }
}

class RootJarTransform(
    override val src: JarInput,
    override val dest: File
) : RootTransform() {
    override
    val format = Format.JAR

    override
    val all = setOf(
        FileTransform(this, src.file, dest)
    )

    override
    fun write(child: BaseTransform) {
        val src = child.src
        val dest = child.dest
//        println("$src -> $dest")
        FileUtils.copyFile(src, dest)
    }

    init {
        FileUtils.deleteRecursivelyIfExists(dest)
        FileUtils.deleteIfExists(dest)
    }
}

sealed
class BaseTransform {
    abstract
    val root: RootTransform
    abstract
    val src: File
    abstract
    val dest: File
}

class DirTransform(
    override val root: RootTransform,
    override val src: File,
    override val dest: File
) : BaseTransform() {
    val children by lazy {
        src.listFiles().map { src ->
            val dest = File(dest, src.name)
            if (src.isDirectory) {
                DirTransform(root, src, dest)
            } else {
                FileTransform(root, src, dest)
            }
        }
    }
    val dirs by lazy {
        children.filterIsInstance<DirTransform>()
    }
    val files by lazy {
        children.filterIsInstance<FileTransform>()
    }
    val all by lazy {
        files + dirs.flatMap {
            it.dirs + it
        }.flatMap {
            it.files
        }
    }
}

class FileTransform(
    override val root: RootTransform,
    override val src: File,
    override val dest: File
) : BaseTransform()

sealed
class RootTransform {
    abstract
    val src: QualifiedContent

    abstract
    val dest: File

    abstract
    val format: Format

    abstract
    val all: Collection<FileTransform>

    val classFileLocator: ClassFileLocator by lazy {
        format.let {
            if (it == DIRECTORY) {
                ClassFileLocator.ForFolder(src.file)
            } else {
                val jarFile = JarFile(src.file)
                ClassFileLocator.ForJarFile(jarFile)
            }
        }
    }

    abstract
    fun write(child: BaseTransform)
}

private const val CLASS_FILE_EXTENSION = ".class"

class Runner(
    delegate: TransformInvocation
) : TransformInvocation by delegate {
    val temp = context.temporaryDir.apply {
        println("temp=$this")
        mkdirs()
    }
    val dirTransformer = transformer(DIRECTORY)
    val jarTransformer = transformer(JAR)
    val ref = referencedInputs.run {
        directoryInputs.map(dirTransformer) + jarInputs.map(jarTransformer)
    }
    val primary = inputs.run {
        directoryInputs.map(dirTransformer) + jarInputs.map(jarTransformer)
    }
    val classFileLocator = (ref + primary).map {
        it.classFileLocator
    }.let {
        ClassFileLocator.Compound(it)
    }
    val typePool: TypePool = TypePool.Default.WithLazyResolution(
        TypePool.CacheProvider.Simple(),
        classFileLocator,
        TypePool.Default.ReaderMode.FAST,
        TypePool.ClassLoading.ofBootPath()
    )
    val classFileVersion = ClassFileVersion.JAVA_V7
    val entryPoint: EntryPoint = EntryPoint.Default.REDEFINE
    fun newByteBuddy(): ByteBuddy =
        entryPoint.byteBuddy(classFileVersion)

    val androidApplication = typePool.describe("evovetech.sample.AndroidApplication")
            .resolve()

    fun run() {
        primary.forEach {
            val root = it
            it.all.forEach {
                when (root) {
                    is RootDirTransform -> it.writeTo(root)
                    else -> root.write(it)
                }
            }
        }
    }

    fun FileTransform.writeTo(root: RootDirTransform) {
        val file = dest.path
        val complete: () -> Unit = {
            root.write(this)
        }
        if (!file.endsWith(CLASS_FILE_EXTENSION)) {
            return complete()
        }
        val typeName = file.replace('/', '.').substring(0, file.length - CLASS_FILE_EXTENSION.length)
        val typeDescription = typePool.describe(typeName).resolve()
        val anno = typeDescription.declaredAnnotations.ofType(androidApplication)
                   ?: return complete()
        println("anno=$anno")
        val byteBuddy = newByteBuddy()
        val methodTransformer = MethodNameTransformer.Suffixing("original")
        val builder = try {
            entryPoint.transform(typeDescription, byteBuddy, classFileLocator, methodTransformer)
        } catch (_: Exception) {
            return complete()
        }

        val dynamicType = builder.defineField("defined", String::class.java)
                .value("one")
                .make()
        try {
            dynamicType.saveIn(root.dest)
        } catch (exception: IOException) {
            throw  RuntimeException("Cannot save $typeName in $root", exception)
        }
    }
}

val File.dirs
    get() = if (isDirectory) {
        emptyList()
    } else {
        listFiles().filter { it.isDirectory }
    }
val File.files
    get() = if (isDirectory) {
        listFiles().filterNot { it.isDirectory }
    } else {
        listOf(this)
    }
