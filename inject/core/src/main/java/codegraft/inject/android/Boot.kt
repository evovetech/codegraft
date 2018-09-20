/*
 * Copyright (C) 2018 evove.tech
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
