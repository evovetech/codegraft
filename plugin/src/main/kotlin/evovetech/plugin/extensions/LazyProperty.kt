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

package evovetech.plugin.extensions

import kotlin.reflect.KProperty

class LazyProperty<T>(
    private val parent: DynamoAware,
    private val initializer: ((thisRef: Any?) -> T)? = null
) {
    operator
    fun <T> getValue(thisRef: Any?, property: KProperty<*>): T {
        val key = property.name
        val p = parent.getProperty(key) ?: initializer?.invoke(thisRef)?.apply {
            parent.setProperty(key, this)
        }
        @Suppress("UNCHECKED_CAST")
        return p as T
    }

    operator
    fun <T> setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        parent.setProperty(property.name, value)
    }
}
