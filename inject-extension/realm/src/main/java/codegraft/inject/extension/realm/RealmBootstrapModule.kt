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

package codegraft.inject.extension.realm

import codegraft.inject.BootScope
import codegraft.inject.android.AndroidApplication
import dagger.Module
import dagger.Provides
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmConfiguration.Builder
import javax.inject.Named

@Module
class RealmBootstrapModule {
    @Provides
    @BootScope
    fun provideRealmConfiguration(
        @BootScope app: AndroidApplication,
        @Named("realmInit") realmInit: RealmInit?
    ): RealmConfiguration {
        Realm.init(app)
        val builder = Builder()
        val realmConfiguration = realmInit?.let { it(builder) } ?: builder.build()
        Realm.setDefaultConfiguration(realmConfiguration)
        return realmConfiguration
    }
}
