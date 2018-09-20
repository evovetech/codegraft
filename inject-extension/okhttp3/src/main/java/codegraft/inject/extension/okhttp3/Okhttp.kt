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

package codegraft.inject.extension.okhttp3

import codegraft.inject.BindPlugin
import codegraft.inject.Plugin
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
@BindPlugin
class Okhttp
@Inject constructor(
    private val okhttpProvider: Provider<OkHttpClient>,
    private val okhttpBuilderProvider: Provider<OkHttpClient.Builder>
) : Plugin {
    val client: OkHttpClient
        get() = okhttpProvider.get()

    val builder: OkHttpClient.Builder
        get() = okhttpBuilderProvider.get()
}

