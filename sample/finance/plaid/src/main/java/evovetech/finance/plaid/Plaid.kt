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

package evovetech.finance.plaid

import codegraft.inject.BootstrapComponent
import codegraft.inject.android.AndroidApplication
import codegraft.inject.extension.okhttp3.OkhttpComponent
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@BootstrapComponent(
    bootstrapDependencies = [OkhttpComponent::class],
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
