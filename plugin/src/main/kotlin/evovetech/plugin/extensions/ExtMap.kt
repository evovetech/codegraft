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

import java.util.function.BiFunction
import java.util.function.Function

class ExtMap : PrefixMap<Any?> {
    constructor(
        prefix: String
    ) : super(prefix)

    constructor(
        prefix: String,
        delegate: MutableMap<String, Any?>
    ) : super(prefix, delegate)
}

open
class PrefixMap<V>(
    prefix: String,
    private val delegate: MutableMap<String, V> = HashMap()
) : MutableMap<String, V> by delegate {
    private
    val prefix = prefix.let {
        if (it.isEmpty()) {
            it
        } else {
            "$it."
        }
    }

    fun loadPrefixed(map: Map<out String, V>) = map
            .filterKeys { it.startsWith(prefix) }
            .apply(this::putAll)

    override
    fun put(key: String, value: V): V? = key.actual.let {
//        println("put $prefix$it=$value")
        delegate.put(it, value)
    }

    override
    fun putAll(from: Map<out String, V>) = from
            .mapKeys { it.key.actual }
            .let(delegate::putAll)

    override
    fun containsKey(key: String) = key.actual.let {
        delegate.containsKey(key.actual).apply {
//            println("containsKey $prefix$it=$this")
        }
    }

    override
    fun get(key: String) = key.actual.let {
        delegate[it].apply {
//            println("get $prefix$it=$this")
        }
    }

    override
    fun getOrDefault(key: String, defaultValue: V): V {
        return delegate.getOrDefault(key.actual, defaultValue)
    }

    override
    fun compute(key: String, p1: BiFunction<in String, in V?, out V?>): V? {
        return delegate.compute(key.actual, p1)
    }

    override
    fun computeIfAbsent(key: String, p1: Function<in String, out V>): V {
        return delegate.computeIfAbsent(key.actual, p1)
    }

    override
    fun computeIfPresent(key: String, p1: BiFunction<in String, in V, out V?>): V? {
        return delegate.computeIfPresent(key.actual, p1)
    }

    override
    fun merge(key: String, p1: V, p2: BiFunction<in V, in V, out V?>): V? {
        return delegate.merge(key.actual, p1, p2)
    }

    override
    fun putIfAbsent(key: String, p1: V): V? {
        return delegate.putIfAbsent(key, p1)
    }

    override
    fun remove(key: String): V? {
        return delegate.remove(key.actual)
    }

    override
    fun remove(key: String, value: V): Boolean {
        return delegate.remove(key.actual, value)
    }

    override
    fun replace(key: String, p1: V, p2: V): Boolean {
        return delegate.replace(key.actual, p1, p2)
    }

    override
    fun replace(key: String, p1: V): V? {
        return delegate.replace(key.actual, p1)
    }

    private
    val String.actual: String
        get() = if (startsWith(prefix)) {
            substring(prefix.length)
        } else {
            this
        }
}
