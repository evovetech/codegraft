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

import android.app.Activity
import android.app.Fragment
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ContentProvider
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjector
import dagger.android.AndroidInjector.Factory
import dagger.android.DispatchingAndroidInjector
import dagger.multibindings.Multibinds
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass

typealias AndroidInjectorMap<T> = Map<KClass<out T>, Factory<out T>>

@Module
interface AndroidInjectModule<T : Any> {
    @Multibinds
    fun bindInjectorFactories(): AndroidInjectorMap<T>

    @Binds
    fun bindInjector(injector: AndroidComponentInjector<T>): AndroidInjector<T>
}

@Singleton
class AndroidComponentInjector<T>
@Inject constructor(
    private val injector: DispatchingAndroidInjector<T>
) : AndroidInjector<T> by injector

@Module
interface AndroidInjectActivityModule :
    AndroidInjectModule<Activity>

@Module
interface AndroidInjectFragmentModule :
    AndroidInjectModule<Fragment>

@Module(includes = [AndroidInjectFragmentModule::class])
interface AndroidInjectSupportFragmentModule :
    AndroidInjectModule<android.support.v4.app.Fragment>

@Module
interface AndroidInjectServiceModule :
    AndroidInjectModule<Service>

@Module
interface AndroidInjectBroadcastReceiverModule :
    AndroidInjectModule<BroadcastReceiver>

@Module
interface AndroidInjectContentProviderModule :
    AndroidInjectModule<ContentProvider>
