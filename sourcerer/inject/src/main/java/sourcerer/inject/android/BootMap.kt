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

package sourcerer.inject.android

import java.util.WeakHashMap

open
class BootMap<out Component : Any>(
    private val build: (AndroidApplication) -> ApplicationBootComponent<Component>
) {
    private
    val map: WeakHashMap<AndroidApplication, ApplicationBootComponent<Component>> = WeakHashMap()

    operator
    fun get(
        key: AndroidApplication
    ): Component = getBoot(key).component

    private
    fun getBoot(
        key: AndroidApplication
    ) = synchronized(map) {
        map.getOrPut(key) {
            build(key)
        }
    }
}
