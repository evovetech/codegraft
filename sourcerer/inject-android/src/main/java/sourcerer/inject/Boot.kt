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

open
class AbstractBootstrap<out T, out B : BootComponent.Builder<T>>(
    boot: B.() -> Unit,
    appInit: T.() -> Unit,
    bootBuilder: () -> B
) : BootComponent<T> {
    private val boot: BootComponent<T> by lazy {
        val builder = bootBuilder()
        builder.boot()
        builder.build()
    }
    final override val component: T by lazy {
        val comp = this.boot.component
        comp.appInit()
        comp
    }

    fun initialize() {
        println("component=$component")
    }
}

interface BootstrapApplication<out T> {
    val bootstrap: BootComponent<T>
}
