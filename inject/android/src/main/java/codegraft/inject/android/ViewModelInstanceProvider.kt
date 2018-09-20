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
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelStore
import android.arch.lifecycle.ViewModelStoreOwner
import android.arch.lifecycle.ViewModelStores
import android.support.v4.app.FragmentActivity
import javax.inject.Inject
import kotlin.reflect.KClass

class ViewModelInstanceProvider
@Inject constructor(
    val factory: ViewModelFactory
) {
    @Volatile private lateinit
    var provider: ViewModelProvider

    fun <VM : ViewModel> get(thisRef: Any?, type: KClass<VM>): VM {
        if (!::provider.isInitialized) {
            val store = thisRef.getViewModelStore()!!
            ViewModelProvider(store, factory).let { p ->
                provider = p
                return p[type.java]
            }
        }

        return provider[type.java]
    }
}

fun Any?.getViewModelStore(): ViewModelStore? = when (this) {
    is ViewModelStoreOwner -> this.viewModelStore
    is FragmentActivity -> ViewModelStores.of(this)
    is SupportFragment -> ViewModelStores.of(this)
    else -> null
}
