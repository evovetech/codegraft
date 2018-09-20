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
