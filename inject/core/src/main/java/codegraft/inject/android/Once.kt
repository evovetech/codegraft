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

package codegraft.inject.android

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater
import kotlin.reflect.KProperty

class Test {
    var say: String by once {
        it?.toString() ?: "uh-oh"
    }

    init {
        say = ""
    }
}

interface Once<T> {
    fun isInitialized(): Boolean
    fun setValue(newValue: T): Boolean
    fun getValue(thisRef: Any?): T
}

typealias Initializer<T> = (thisRef: Any?) -> T

fun <T> once(
    initializer: Initializer<T>
): Once<T> = OnceImpl(initializer)

operator
fun <T> Once<T>.getValue(thisRef: Any?, property: KProperty<*>): T {
    return getValue(thisRef)
}

operator
fun <T> Once<T>.setValue(thisRef: Any?, property: KProperty<*>, value: T) {
    setValue(value)
}

private
class OnceImpl<T>(
    initializer: Initializer<T>
) : Once<T> {

    @Volatile private
    var _initializer: Initializer<T>? = initializer

    @Volatile private
    var _value: Any? = UninitializedValue

    // this final field is required to enable safe publication of constructed instance
    private
    val final: Any = UninitializedValue

    override
    fun getValue(thisRef: Any?): T {
        _value.ifInitialized { value ->
            @Suppress("UNCHECKED_CAST")
            return value as T
        }

        // if we see null in initializer here it means that the value is already set by another thread.
        // So we can skip this block
        _initializer.ifNotNull { initializer: Initializer<T> ->
            val newValue = initializer(thisRef)
            if (setValue(newValue)) {
                _initializer = null
                return newValue
            }
        }

        @Suppress("UNCHECKED_CAST")
        return _value as T
    }

    override
    fun setValue(newValue: T): Boolean {
        return valueUpdater.compareAndSet(this, UninitializedValue, newValue)
    }

    override
    fun isInitialized(): Boolean {
        _value.ifInitialized {
            return true
        }
        return false
    }

    override
    fun toString(): String {
        _value.ifInitialized { value ->
            return value.toString()
        }
        return "Once value not initialized yet."
    }

    companion object {
        private
        val valueUpdater = AtomicReferenceFieldUpdater.newUpdater(
            OnceImpl::class.java,
            Any::class.java,
            "_value"
        )
    }
}

internal
object UninitializedValue

internal inline
fun <T> T.ifNotInitialized(block: (T) -> Unit) {
    if (this === UninitializedValue) {
        block(this)
    }
}

internal inline
fun <T> T.ifInitialized(block: (T) -> Unit) {
    if (this !== UninitializedValue) {
        block(this)
    }
}

internal inline
fun <T : Any> T?.ifNotNull(block: (T) -> Unit) {
    if (this != null) {
        block(this)
    }
}
