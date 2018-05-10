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

package evovetech.util

import kotlin.LazyThreadSafetyMode.PUBLICATION
import kotlin.reflect.KProperty

class Once<T>(
    private val initializer: (thisRef: Any) -> T
) {
    @Volatile private
    var _value: T? = null
    @Volatile internal
    var thisRef: Any? = null

    private
    val _finalValue: T by lazy(PUBLICATION) {
        _value ?: initializer(thisRef!!)
    }

    var value: T
        get() = _finalValue
        set(value) {
            _value = value
        }
}

fun <T> once(
    initializer: (thisRef: Any) -> T
): Once<T> = Once(initializer)

operator
fun <T> Once<T>.getValue(thisRef: Any?, property: KProperty<*>): T {
    thisRef?.also {
        this.thisRef = it
    }
    return value
}

operator
fun <T> Once<T>.setValue(thisRef: Any?, property: KProperty<*>, value: T) {
    thisRef?.also {
        this.thisRef = it
    }
    this.value = value
}
