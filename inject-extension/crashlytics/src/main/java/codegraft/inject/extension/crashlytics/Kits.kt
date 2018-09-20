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

import io.fabric.sdk.android.Fabric
import io.fabric.sdk.android.Kit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass

@Singleton
class Kits
@Inject constructor(
    fabric: Fabric
) {
    private val map = fabric.kits
            .groupBy { it::class }
            .mapValues { it.value.first()!! }

    operator
    fun <T : Kit<*>> get(clazz: KClass<T>): T {
        val kit = map[clazz]
        return clazz.java.cast(kit)
    }

    inline
    fun <reified T : Kit<*>> get(): T {
        return get(T::class)
    }
}
