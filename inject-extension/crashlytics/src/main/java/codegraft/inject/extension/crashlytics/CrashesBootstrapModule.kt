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

package codegraft.inject.extension.crashlytics

import codegraft.inject.BootScope
import codegraft.inject.android.AndroidApplication
import dagger.Module
import dagger.Provides
import io.fabric.sdk.android.Fabric
import io.fabric.sdk.android.Fabric.Builder
import javax.inject.Named

typealias FabricInit = Fabric.Builder.() -> Fabric

@Module
class CrashesBootstrapModule {
    @Provides
    @BootScope
    fun provideFabric(
        @BootScope app: AndroidApplication,
        @Named("fabric") init: FabricInit?
    ): Fabric {
        val builder = Builder(app)
        val fabric = init?.run { invoke(builder) }
                     ?: builder.build()
        return Fabric.with(fabric)
    }
}
