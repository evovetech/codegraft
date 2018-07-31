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

import android.app.Application
import codegraft.inject.BindPlugin
import codegraft.inject.BootScope
import codegraft.inject.BootstrapComponent
import codegraft.inject.Plugin
import codegraft.inject.android.AndroidApplication
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

typealias OkHttpInit = OkHttpClient.Builder.(app: Application) -> OkHttpClient

@BootstrapComponent(
    applicationModules = [ClientModule::class],
    bootstrapModules = [OkHttpBuilderModule::class]
)
interface ClientComponent {
    val client: Client
}

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

@Module
class ClientModule {
    @Provides
    fun provideDefaultOkhttpBuilder(
        okhttp: OkHttpClient
    ): OkHttpClient.Builder {
        return okhttp.newBuilder()
    }
}

@Module
class OkHttpBuilderModule {
    @Provides
    @BootScope
    fun provideDefaultOkhttp(
        @BootScope app: AndroidApplication,
        @Named("okhttp") init: OkHttpInit
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
        builder.init(app)
        return builder.build()
    }
}

