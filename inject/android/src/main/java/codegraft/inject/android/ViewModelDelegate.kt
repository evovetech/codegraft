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

import android.arch.lifecycle.ViewModel
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class ViewModelDelegate<VM : ViewModel>(
    private val viewModelType: KClass<VM>,
    private val provider: () -> ViewModelInstanceProvider
) : ReadOnlyProperty<Any?, VM> {
    @Volatile private lateinit
    var viewModel: VM

    override operator
    fun getValue(thisRef: Any?, property: KProperty<*>): VM {
        if (!this::viewModel.isInitialized) {
            provider().get(thisRef, viewModelType).let { vm ->
                viewModel = vm
                return vm
            }
        }
        return viewModel
    }
}

inline
fun <reified VM : ViewModel> (() -> ViewModelInstanceProvider).delegate(): ViewModelDelegate<VM> {
    return ViewModelDelegate(VM::class, this)
}
