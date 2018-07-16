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

package sourcerer.inject

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
