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

package evovetech.blog.medium

import org.mockito.Mockito
import java.io.File

val ModulePath: String by lazy {
    ":blog:medium".split(":")
            .filter(String::isNotEmpty)
            .joinToString(prefix = "/")
}
val Module: File by lazy(ModulePath::moduleDir)

inline
fun <reified T : Any> mock(
    noinline init: (T) -> Unit = {}
) = lazy {
    Mockito.mock(T::class.java)
            .apply(init)
}

val String.moduleDir: File
    get() {
        val relDir = File(this)
        val curDir = File(".")
                .absoluteFile
                .parentFile
        return if (relDir.name.equals(curDir.name, ignoreCase = true)) {
            curDir
        } else {
            File(curDir, relDir.path)
        }
    }
