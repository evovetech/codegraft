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

typealias AndroidInjectorMap<T> = Map<Class<out T>, Factory<out T>>
typealias SupportFragment = android.support.v4.app.Fragment

@Module
interface AndroidInjectModule<T : Any> {
    @Multibinds
    fun bindInjectorFactories(): AndroidInjectorMap<T>

    @Binds
    fun bindInjector(injector: DispatchingAndroidInjector<T>): AndroidInjector<T>
}

@Module
interface AndroidInjectActivityModule :
    AndroidInjectModule<Activity>

@Module(includes = [AndroidInjectActivityModule::class])
interface AndroidInjectFragmentModule :
    AndroidInjectModule<Fragment>

@Module(includes = [AndroidInjectFragmentModule::class])
interface AndroidInjectSupportFragmentModule :
    AndroidInjectModule<SupportFragment>

@Module
interface AndroidInjectServiceModule :
    AndroidInjectModule<Service>

@Module
interface AndroidInjectBroadcastReceiverModule :
    AndroidInjectModule<BroadcastReceiver>

@Module
interface AndroidInjectContentProviderModule :
    AndroidInjectModule<ContentProvider>

@Module
interface AndroidInjectApplicationModule :
    AndroidInjectModule<AndroidApplication>
