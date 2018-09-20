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

@file:JvmName("Mocks")

package evovetech.blog.medium

import android.content.res.AssetFileDescriptor
import android.util.Log
import org.mockito.Mockito.`when`
import org.mockito.invocation.InvocationOnMock
import java.io.File
import java.io.FileOutputStream

fun mockAssetFd(file: File): Lazy<AssetFileDescriptor> = mock { afd ->
    `when`(afd.createOutputStream()).thenAnswer {
        Log.d("mockAssetFd", "output from file: $file")
        if (!file.exists()) {
            file.parentFile.mkdirs()
        }
        FileOutputStream(file)
    }
}

inline
fun <reified T> InvocationOnMock.arg(index: Int): T? =
    arguments[index] as? T

inline
fun <reified T> InvocationOnMock.arg1(): T? =
    arg(0)
