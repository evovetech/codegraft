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

interface BootComponent<out Component : Any> {
    val component: Component

    interface Builder<out Boot : BootComponent<*>> :
        codegraft.inject.Builder<Boot>
}

interface BootApplication<out Component : Any> {
    val bootstrap: Bootstrap<Component>
}

val <Component : Any> BootApplication<Component>.component: Component
    get() = bootstrap.component

class Bootstrap<out Component : Any>(
    buildFunc: () -> Component
) : BootComponent<Component> {
    override
    val component by lazy(buildFunc)
}
