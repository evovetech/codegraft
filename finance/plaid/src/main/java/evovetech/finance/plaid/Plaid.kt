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

package evovetech.finance.plaid

import dagger.Module
import dagger.Provides
import evovetech.sample.network.ClientComponent
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import sourcerer.inject.BootstrapComponent
import sourcerer.inject.android.AndroidApplication
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@BootstrapComponent(
    bootstrapDependencies = [ClientComponent::class],
    applicationModules = [PlaidModule::class]
)
interface PlaidComponent {
    val client: PlaidClient
}

@Singleton
class PlaidClient
@Inject constructor(
    @Named("plaid") private val okhttp: OkHttpClient,
    @Named("plaid") private val retrofit: Retrofit,
    private val service: PlaidService
) : PlaidService by service

@Module
class PlaidModule {
    @Provides
    @Singleton
    @Named("plaid")
    fun provideOkhttp(
        app: AndroidApplication,
        okhttpBuilder: OkHttpClient.Builder
    ): OkHttpClient {
        // TODO:
        return okhttpBuilder.build()
    }

    @Provides
    @Singleton
    @Named("plaid")
    fun provideRetrofit(
        app: AndroidApplication,
        @Named("plaid") client: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
                .client(client)
                .baseUrl("https://sandbox.plaid.com/")
                .build()
    }

    @Provides
    @Singleton
    fun providePlaidService(
        @Named("plaid") retrofit: Retrofit
    ): PlaidService {
        return retrofit.create(PlaidService::class.java)
    }
}

interface PlaidService {
    @GET("institutions/get")
    fun listInstitutions(): Call<Any>
}
