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

@Deprecated("moved")
typealias KeyProvider<K, V> = KeyProviderMap<K, V>

@Deprecated("moved")
typealias ClassKeyProvider<T> = ClassKeyProviderMap<T>

inline
fun <T, R> Provider<T>.map(
    block: (T) -> R
) = get().let(block)

inline
fun <T, P : Provider<T>> P.with(
    block: T.() -> Unit
): P {
    get().apply(block)
    return this
}

inline
fun <T : Any, R> Provider<T>?.fold(
    success: (T) -> R,
    failure: () -> R
) = this?.map(success) ?: failure()
