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
import sourcerer.io.Writeable
import java.util.ArrayList
import javax.annotation.processing.Filer

interface BaseElements<E : BaseElement> :
    MutableMap<ClassName, E>,
    Writeable {

    fun create(key: ClassName): E

    fun writeJavaFiles(filer: Filer): Any
}

fun <E : BaseElement> BaseElements<E>.entryList() =
    ArrayList(entries)

fun <E : BaseElement> BaseElements<E>.getOrCreate(key: ClassName) =
    getOrPut(key) { create(key) }
