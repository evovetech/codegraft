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
import codegraft.inject.BootScope
import codegraft.inject.android.AndroidApplication
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.OkHttpClient.Builder
import javax.inject.Named

typealias OkHttpInit = OkHttpClient.Builder.(app: Application) -> OkHttpClient

@Module
class OkhttpBootstrapModule {
    @Provides
    @BootScope
    fun provideDefaultOkhttp(
        @BootScope app: AndroidApplication,
        @Named("okhttp") init: OkHttpInit
    ): OkHttpClient {
        val builder = Builder()
        builder.init(app)
        return builder.build()
    }
}
