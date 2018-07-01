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

interface BootComponent<out Component : Any> {
    val component: Component

    interface Builder<out Boot : BootComponent<*>> :
        sourcerer.inject.android.Builder<Boot>
}

interface BootApplication<out Component : Any> {
    val bootstrap: Bootstrap<Component>
}

val <Component : Any> BootApplication<Component>.component: Component
    get() = bootstrap.component

open
class Bootstrap<out Component : Any>(
    builder: BootBuilder<Component>
) : BootComponent<Component> {
    final override
    val component by lazy(builder::build)

    internal
    fun initialize() {
        val comp = component
        println("component=$comp")
    }

    data
    class Builder<out Component : Any>(
        val application: AndroidApplication,
        val buildFunc: () -> Component
    ) {
        constructor(
            application: AndroidApplication,
            builder: BootComponent.Builder<BootComponent<Component>>
        ) : this(application, {
            builder.build().component
        })

        internal
        fun build(): Component = buildFunc().apply {
            when (this) {
                is HasApplicationInjector -> applicationInjector.inject(application)
            }
        }
    }
}

typealias BootBuilder<Component> = () -> Bootstrap.Builder<Component>

fun <Component : Any> BootBuilder<Component>.build(): Component = invoke()
        .build()
