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

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelStore
import android.arch.lifecycle.ViewModelStoreOwner
import android.arch.lifecycle.ViewModelStores
import android.support.v4.app.FragmentActivity
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.Multibinds
import sourcerer.inject.BootstrapComponent
import sourcerer.inject.ClassKeyProviderMap
import sourcerer.inject.ClassMap
import sourcerer.inject.ClassProviderMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

typealias ViewModelMap = ClassMap<ViewModel>
typealias ViewModelProviderMap = ClassProviderMap<ViewModel>

@BootstrapComponent(
    applicationModules = [ViewModelComponentModule::class],
    autoInclude = false,
    flatten = true
)
interface ViewModelComponent

@Module
interface ViewModelComponentModule {
    @Multibinds
    fun bindViewModelProviderMap(): ViewModelMap

    @Binds
    fun bindViewModelFactory(viewModelFactory: ViewModelFactory): ViewModelProvider.Factory
}

class ViewModelInstanceProvider<T : Any>
@Inject constructor(
    val factory: ViewModelProvider.Factory,
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

inline
fun <reified VM : ViewModel> viewModelDelegate(
    noinline provider: () -> ViewModelInstanceProvider<*>
): ViewModelDelegate<VM> {
    return ViewModelDelegate(VM::class, provider)
}

class ViewModelDelegate<VM : ViewModel>(
    private val viewModelType: KClass<VM>,
    private val providerFunc: () -> ViewModelInstanceProvider<*>
) {
    private
    val viewModel: VM by lazy {
        providerFunc()[viewModelType]
    }

    operator
    fun getValue(thisRef: Any?, property: KProperty<*>): VM {
        return viewModel
    }
}

@Singleton
class ViewModelFactory
@Inject constructor(
    override val providers: ViewModelProviderMap
) : ClassKeyProviderMap<ViewModel>,
    ViewModelProvider.Factory {
    override
    fun <T : ViewModel> create(modelClass: Class<T>): T {
        return get(modelClass)!!
    }
}

@MapKey
@MustBeDocumented
@Retention(RUNTIME)
annotation
class ViewModelKey(
    val value: KClass<out ViewModel>
)

fun Any.getViewModelStore(): ViewModelStore? = when (this) {
    is ViewModelStoreOwner -> this.viewModelStore
    is FragmentActivity -> ViewModelStores.of(this)
    is SupportFragment -> ViewModelStores.of(this)
    else -> null
}
