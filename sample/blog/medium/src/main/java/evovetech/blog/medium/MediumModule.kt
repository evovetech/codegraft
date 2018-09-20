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

import codegraft.inject.android.AndroidApplication
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.OkHttpClient.Builder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

private const
val AuthKey = "Bearer ${BuildConfig.API_KEY}"

@Module(subcomponents = [MediumUserComponent::class])
class MediumModule {
    @Provides
    @Singleton
    @Named("medium")
    fun provideOkhttp(
        app: AndroidApplication,
        okhttpBuilder: Builder
    ): OkHttpClient {
        return okhttpBuilder
                .addNetworkInterceptor { chain ->
                    val request = chain.request()
                            .newBuilder()
                            .header("Authorization", AuthKey)
                            .build()
                    chain.proceed(request)
                }
                .build()
    }

    @Provides
    @Named("medium")
    fun provideGson(): Gson {
        return GsonBuilder()
                // TODO:
                .create()
    }

    @Provides
    @Singleton
    @Named("medium")
    fun provideRetrofit(
        app: AndroidApplication,
        @Named("medium") client: OkHttpClient,
        @Named("medium") gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
                .client(client)
                .baseUrl("https://api.medium.com/v1/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
    }

    @Provides
    @Singleton
    fun provideMediumService(
        @Named("medium") retrofit: Retrofit
    ): MediumService {
        return retrofit.create(MediumService::class.java)
    }
}
