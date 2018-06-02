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

package evovetech.sample.network

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import okhttp3.OkHttpClient
import sourcerer.inject.Bootstrap
import sourcerer.inject.Plugin
import sourcerer.inject.PluginKey
import sourcerer.inject.Plugins
import javax.inject.Inject
import javax.inject.Singleton

@Bootstrap.Component(modules = [ClientPlugin::class])
interface ClientComponent {
    val plugins: Plugins
}

@Singleton
class Client
@Inject constructor(
    val okhttp: OkHttpClient
) : Plugin

@Module(includes = [ClientModule::class])
abstract
class ClientPlugin {
    @Binds
    @IntoMap
    @PluginKey(Client::class)
    abstract fun bindClient(client: Client): Plugin
}

@Module
class ClientModule {
    @Provides
    @Singleton
    fun provideOkhttp(): OkHttpClient {
        return OkHttpClient()
    }
}

