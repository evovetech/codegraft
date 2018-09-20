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

import android.app.Application
import android.util.Log

// Content Provider
class BootstrapProvider : EmptyContentProvider() {
    override
    fun onCreate(): Boolean {
        val TAG = "BootstrapProvider"
        val app = context as Application
        when (app) {
            is BootApplication<*> -> {
                Log.d(TAG, "Bootstrapping!!")
                val component = app.bootstrap.component
                if (component is HasApplicationInjector) {
                    component.applicationInjector.inject(app)
                }
            }
            else -> {
                Log.d(TAG, "NO Bootstraps :(")
            }
        }
        return true
    }
}
