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

package evovetech.gradle.transform

import android.app.Application
import com.android.build.api.transform.TransformInvocation
import evovetech.codegen.OnCreate
import evovetech.gradle.transform.content.Output
import evovetech.gradle.transform.content.allFiles
import evovetech.gradle.transform.content.classFileLocator
import net.bytebuddy.ByteBuddy
import net.bytebuddy.ClassFileVersion
import net.bytebuddy.build.EntryPoint
import net.bytebuddy.build.EntryPoint.Default.REBASE
import net.bytebuddy.description.type.TypeDescription
import net.bytebuddy.dynamic.ClassFileLocator.Compound
import net.bytebuddy.dynamic.scaffold.inline.MethodNameTransformer.Suffixing
import net.bytebuddy.implementation.MethodDelegation
import net.bytebuddy.matcher.ElementMatchers.named
import net.bytebuddy.pool.TypePool
import net.bytebuddy.pool.TypePool.CacheProvider.Simple
import net.bytebuddy.pool.TypePool.ClassLoading
import net.bytebuddy.pool.TypePool.Default.ReaderMode.FAST
import net.bytebuddy.pool.TypePool.Default.WithLazyResolution
import java.io.File
import kotlin.reflect.KClass

class ByteBuddyRunRun(
    bootClasspath: () -> List<File>,
    delegate: TransformInvocation
) : RunRun(delegate) {
    private
    val bootClassFileLocator by lazy {
        Compound(bootClasspath().map { it.classFileLocator })
    }
    val classFileLocator by lazy {
        (refInputs + primaryInputs).map {
            it.classFileLocator
        }.let {
            Compound(it + bootClassFileLocator)
        }
    }
    val typePool: TypePool by lazy {
        WithLazyResolution(
            Simple(),
            classFileLocator,
            FAST,
            ClassLoading.ofBootPath()
        )
    }
    val classFileVersion = ClassFileVersion.JAVA_V7
    val entryPoint: EntryPoint = REBASE
    val androidApplication by lazy {
        typePool.resolve<Application>()
    }

    fun newByteBuddy(): ByteBuddy =
        entryPoint.byteBuddy(classFileVersion)

    override
    fun run() {
        println("bytebuddy runrun! start")
        try {
            transforms.flatMap { it.output.allFiles() }
                    .forEach(this::write)
        } finally {
            println("bytebuddy runrun! complete")
        }
    }

    private
    fun write(output: Output) {
        val src = output.input.rel.path
//        val path = output.rel.path
        val copy: () -> Unit = {
            output.copyToDest()
        }
        if (!src.endsWith(CLASS_FILE_EXTENSION)) {
            println("src=$src")
            return copy()
        }

        val typeName = src.replace('/', '.')
                .substring(0, src.length - CLASS_FILE_EXTENSION.length)
        val typeDescription = typePool.describe(typeName).resolve()
        if (!typeDescription.isAssignableTo(androidApplication)) {
            println("type=$typeDescription")
            return copy()
        }
        val byteBuddy = newByteBuddy()
        val methodTransformer = Suffixing("original")
        try {
            val maps = entryPoint.transform(typeDescription, byteBuddy, classFileLocator, methodTransformer)
                    .defineProperty("defined", String::class.java)
                    .field(named("defined")).value("one")
                    .method(named("onCreate")).intercept(methodDelegation<OnCreate>())
                    .make()
                    .saveIn(output.base)
            println("maps=$maps")
        } catch (exception: Throwable) {
            exception.printStackTrace()
            return copy()
        }
    }
}

inline
fun <reified T> TypePool.resolve() =
    resolve(T::class)

fun TypePool.resolve(type: KClass<*>): TypeDescription = describe(type.java.canonicalName)
        .resolve()

inline
fun <reified T> methodDelegation() =
    MethodDelegation.to(T::class.java)
