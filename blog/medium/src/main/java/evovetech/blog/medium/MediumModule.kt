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

package evovetech.blog.medium

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import evovetech.blog.medium.ui.medium.MediumModule.DaggerModule
import okhttp3.OkHttpClient
import okhttp3.OkHttpClient.Builder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import sourcerer.inject.android.AndroidApplication
import javax.inject.Named
import javax.inject.Singleton

@Module(includes = [DaggerModule::class])
class MediumModule {
    @Provides
    @Singleton
    @Named("medium")
    fun provideOkhttp(
        app: AndroidApplication,
        okhttpBuilder: Builder
    ): OkHttpClient {
        // TODO:
        return okhttpBuilder.build()
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
