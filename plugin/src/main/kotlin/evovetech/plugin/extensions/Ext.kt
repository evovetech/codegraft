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

import evovetech.plugin.extras
import kotlin.reflect.KProperty

open
class Ext<P : Any>(
    val project: P,
    prefix: String = "",
    delegate: MutableMap<String, Any?> = HashMap(),
    final override val properties: ExtMap = ExtMap(prefix, delegate)
) : DynamoAware, MutableMap<String, Any?> by properties {
    override
    val dynamo: Dynamo by lazy {
        Dynamo(this)
    }

    init {
        project.extras?.properties?.let(properties::loadPrefixed)
    }

    fun put(
        entry: Map.Entry<String, Any?>
    ): Any? = put(entry.key, entry.value)
}

fun <T> ext(prefix: String, initializer: ((thisRef: Any?) -> T)? = null) =
    ExtraProperty(prefix, initializer)

fun <T> ext(initializer: ((thisRef: Any?) -> T)? = null) =
    ExtraProperty("", initializer)

class ExtraProperty<T>(
    val prefix: String,
    val initializer: ((thisRef: Any?) -> T)? = null
) {
    private
    fun key(property: KProperty<*>) = if (prefix.isEmpty()) {
        property.name
    } else {
        "$prefix.${property.name}"
    }

    operator
    fun <T> getValue(thisRef: Any?, property: KProperty<*>): T {
        val key = key(property)
        val p = thisRef?.extras?.properties?.get(key) ?: initializer?.invoke(thisRef)?.apply {
            setValue(thisRef, property, this)
        }
        @Suppress("UNCHECKED_CAST")
        return p as T
    }

    operator
    fun <T> setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        val key = key(property)
        thisRef?.extras?.properties?.set(key, value)
    }
}
