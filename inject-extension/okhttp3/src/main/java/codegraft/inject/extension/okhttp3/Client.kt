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

package codegraft.inject.extension.okhttp3

import codegraft.inject.BindPlugin
import codegraft.inject.Plugin
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
@BindPlugin
class Client
@Inject constructor(
    private val okhttpProvider: Provider<OkHttpClient>,
    private val okhttpBuilderProvider: Provider<OkHttpClient.Builder>
) : Plugin {
    val okhttp: OkHttpClient
        get() = okhttpProvider.get()

    val okhttpBuilder: OkHttpClient.Builder
        get() = okhttpBuilderProvider.get()
}

