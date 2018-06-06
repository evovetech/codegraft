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

    interface Builder<out App, out Boot : BootComponent<App>> {
        fun build(): Boot
    }
}

open
class AbstractBootstrap<
        App,
        Boot : BootComponent<App>,
        BootBuilder : BootComponent.Builder<App, Boot>
        >(
    boot: BootBuilder.() -> Unit,
    appInit: App.() -> Unit,
    bootBuilder: () -> BootBuilder
) {
    private val boot: Boot by lazy {
        val builder = bootBuilder()
        builder.boot()
        builder.build()
    }
    val component: App by lazy {
        val comp = this.boot.component
        comp.appInit()
        comp
    }

    fun initialize() {
        println("component=$component")
    }
}

interface BootstrapApplication<T> {
    val bootstrap: AbstractBootstrap<T, *, *>
}
