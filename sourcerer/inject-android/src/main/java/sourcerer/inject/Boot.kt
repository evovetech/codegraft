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

interface BootComponent<out T> {
    val component: T

    interface Builder<out T> {
        fun build(): BootComponent<T>
    }
}

interface BootstrapApplication<out T> {
    val bootstrap: Bootstrap<T>
}

interface Bootstrap<out T> : BootComponent<T> {
    fun initialize()
}

open
class AbstractBootstrap<out T, out B : BootComponent.Builder<T>>(
    creator: () -> T
) : Bootstrap<T> {
    constructor(
        boot: B.() -> Unit,
        appInit: T.() -> Unit,
        bootBuilder: () -> B
    ) : this({
        bootBuilder()
                .apply(boot)
                .build()
                .component
                .apply(appInit)
    })

    final override
    val component: T by lazy(creator)

    override
    fun initialize() {
        println("component=$component")
    }
}
