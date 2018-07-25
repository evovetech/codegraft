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
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelStore
import android.arch.lifecycle.ViewModelStoreOwner
import android.arch.lifecycle.ViewModelStores
import android.support.v4.app.FragmentActivity
import javax.inject.Inject
import kotlin.reflect.KClass

class ViewModelInstanceProvider<T : Any>
@Inject constructor(
    val factory: ViewModelFactory,
    val instance: T
) {
    val provider: ViewModelProvider by lazy {
        val store = instance.getViewModelStore()!!
        ViewModelProvider(store, factory)
    }

    operator
    fun <VM : ViewModel> get(type: KClass<VM>): VM {
        return provider.get(type.java)
    }

    inline
    fun <reified VM : ViewModel> get(): VM {
        return this[VM::class]
    }
}

fun Any.getViewModelStore(): ViewModelStore? = when (this) {
    is ViewModelStoreOwner -> this.viewModelStore
    is FragmentActivity -> ViewModelStores.of(this)
    is SupportFragment -> ViewModelStores.of(this)
    else -> null
}
