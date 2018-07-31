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

package codegraft.inject.extension.retrofit2

import codegraft.inject.BootScope
import codegraft.inject.BootstrapComponent
import codegraft.inject.android.AndroidApplication
import codegraft.inject.extension.okhttp3.OkhttpComponent
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.Retrofit.Builder

@BootstrapComponent(
    bootstrapDependencies = [OkhttpComponent::class],
    bootstrapModules = [RetrofitModule::class],
    autoInclude = false
)
interface RetrofitComponent {
    val retrofit: Retrofit
}

interface RetrofitBuilder {
    fun Retrofit.Builder.build(
        okhttp: OkHttpClient,
        app: AndroidApplication
    ): Retrofit
}

internal
class RetrofitBuilderImpl(
    private val retrofitInit: RetrofitInit
) : RetrofitBuilder {
    override
    fun Builder.build(okhttp: OkHttpClient, app: AndroidApplication): Retrofit {
        return retrofitInit(this, okhttp, app)
    }
}

interface RetrofitInit : (
    Retrofit.Builder,
    OkHttpClient,
    AndroidApplication
) -> Retrofit

fun RetrofitInit.toBuilder(): RetrofitBuilder {
    return RetrofitBuilderImpl(this)
}

@Module
class RetrofitModule {
    @Provides
    @BootScope
    fun provideRetrofit(
        app: AndroidApplication,
        okhttp: OkHttpClient,
        builder: RetrofitBuilder
    ): Retrofit {
        val retrofit = Retrofit.Builder()
        return builder.run {
            retrofit.build(okhttp, app)
        }
    }
}
