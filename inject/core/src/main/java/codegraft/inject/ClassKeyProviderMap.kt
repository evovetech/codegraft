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

package codegraft.inject

import javax.inject.Provider

open
class ClassKeyProviderMap<T : Any>(
    override val providers: ClassProviderMap<T>
) : ProviderMap<Class<out T>, T>

operator
fun <T : Any, R : T> ClassKeyProviderMap<in T>.get(key: Class<R>): R? {
    return providers[key]?.get()?.let {
        key.castOrNull(it)
    }
}

inline
fun <reified T : Any> ClassKeyProviderMap<in T>.get(): T? =
    get(T::class.java)

inline
fun <reified T : Any> ClassKeyProviderMap<in T>.with(
    block: T.() -> Unit
) = provider()?.with(block)

inline
fun <reified T : Any> ClassKeyProviderMap<in T>.provider(): Provider<T>? =
    providers[T::class.java].castOrNull()
