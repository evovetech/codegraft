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

typealias AndroidApplication = android.app.Application

interface AppComponent<in Application : AndroidApplication> {
    fun inject(application: Application)
}

interface BootComponent<out Component : AppComponent<*>> {
    val component: Component

    interface Builder<out Boot : BootComponent<*>> : sourcerer.inject.android.Builder<Boot> {
        override
        fun build(): Boot
    }
}

interface BootApplication<out Component : AppComponent<*>> {
    val bootstrap: Bootstrap<Component>
}

val <Component : AppComponent<*>> BootApplication<Component>.component: Component
    get() = bootstrap.component

open
class Bootstrap<out Component : AppComponent<*>>(
    builder: () -> Builder<*, Component>
) : BootComponent<Component> {
    final override
    val component by lazy(builder::build)

    internal
    fun initialize() {
        val comp = component
        println("component=$comp")
    }

    data
    class Builder<Application : AndroidApplication, out Component : AppComponent<Application>>(
        val application: Application,
        val builder: sourcerer.inject.android.BootComponent.Builder<BootComponent<Component>>
    ) {
        internal
        fun build(): Component = builder.build().component.apply {
            inject(application)
        }
    }
}

typealias BootBuilder<Component> = () -> Bootstrap.Builder<*, Component>

fun <Component : AppComponent<*>> BootBuilder<Component>.build(): Component = invoke()
        .build()
