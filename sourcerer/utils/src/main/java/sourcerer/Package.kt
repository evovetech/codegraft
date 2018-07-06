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

package sourcerer

import com.squareup.javapoet.ClassName

interface Package {
    val name: String
    val App: Klass
    val AppModule: Klass
    val AppComponent: Klass

    open
    class SubPackage(
        parent: Package,
        name: String
    ) : Package by parent {
        override
        val name = parent.plus(name).name
    }
}

fun ClassName.toPackage(): Package =
    DefaultPackage(this.packageName())

fun String.toPackage(): Package =
    DefaultPackage(this)

operator fun Package.plus(name: String) = "${this.name}.$name"
        .toPackage()

fun Package.className(
    simpleName: String,
    vararg simpleNames: String
) = ClassName.get(name, simpleName, *simpleNames)!!

fun Package.klass(
    simpleName: String,
    vararg simpleNames: String
) = className(simpleName, *simpleNames)
        .toKlass()

open
class DefaultPackage(
    override val name: String
) : Package {
    override
    val App = klass("App")
    override
    val AppModule = klass("AppModule")
    override
    val AppComponent = klass("AppComponent")
}
