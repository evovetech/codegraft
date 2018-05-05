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

import org.gradle.api.Plugin
import org.gradle.api.Project
import kotlin.reflect.full.memberFunctions

class ByteBuddyPlugin : Plugin<Project> {
    override
    fun apply(project: Project) {
//        project.extensions.
        project.plugins.withId("com.android.application") {
            println("plugin=$this")
            project.extensions
                    .findByName("android")
//                ?.apply { println("android extension = ${this::class.java}") }
//                ?.let {
//                    //                    val type = AppExtension::class.java
//                    if (type.isInstance(it)) {
//                        type.cast(it)
//                    } else {
//                        null
//                    }
//                }
                    ?.run(this@ByteBuddyPlugin::setup)
        }
    }

    private
    fun setup(android: Any): Unit = android.run {
        println("android extension = $this")
//        android::class.java.
        val r = android::class.memberFunctions
                .filter { it.name == "registerTransform" }
                .onEach { register ->
                    println("register=$register")
                }
                .first()
        r.call(android, ByteBuddyTransform(), emptyArray<Any>())
//        register?.call(android, ByteBuddyTransform())
//        register.invoke(android, ByteBuddyTransform())
//        registerTransform(ByteBuddyTransform())
    }
}
