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

interface ApplicationBootComponent<out ApplicationComponent : Any> {
    val component: ApplicationComponent

    interface Builder<out Boot : ApplicationBootComponent<*>> :
        sourcerer.inject.android.Builder<Boot>
}

interface ApplicationInjector<in Application : AndroidApplication> {
    fun inject(application: Application)

    interface BootComponent<out ApplicationComponent : ApplicationInjector<*>> :
        ApplicationBootComponent<ApplicationComponent> {

        interface Builder<out Boot : BootComponent<*>>
            : ApplicationBootComponent.Builder<Boot>
    }
}

typealias AppComponent<T> = ApplicationInjector<T>
typealias BootComponent<T> = ApplicationInjector.BootComponent<T>
typealias BootComponentBuilder<T> = ApplicationInjector.BootComponent.Builder<T>

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
        val buildFunc: () -> BootComponent<Component>
    ) {
        constructor(
            application: Application,
            builder: BootComponentBuilder<BootComponent<Component>>
        ) : this(application, builder::build)

        internal
        fun build(): Component = buildFunc().component.apply {
            inject(application)
        }
    }
}

typealias BootBuilder<Component> = () -> Bootstrap.Builder<*, Component>

fun <Component : AppComponent<*>> BootBuilder<Component>.build(): Component = invoke()
        .build()
