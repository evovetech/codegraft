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
