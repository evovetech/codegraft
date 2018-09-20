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
