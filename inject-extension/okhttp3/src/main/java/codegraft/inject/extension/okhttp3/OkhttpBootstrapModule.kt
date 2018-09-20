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
