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

import codegraft.inject.BootstrapComponent
import dagger.android.HasActivityInjector
import dagger.android.HasBroadcastReceiverInjector
import dagger.android.HasContentProviderInjector
import dagger.android.HasFragmentInjector
import dagger.android.HasServiceInjector
import dagger.android.support.HasSupportFragmentInjector

@BootstrapComponent(
    applicationModules = [AndroidInjectApplicationModule::class],
    autoInclude = false,
    flatten = true
)
interface ApplicationInjectorComponent : HasApplicationInjector

@BootstrapComponent(
    bootstrapDependencies = [ViewModelComponent::class],
    applicationModules = [AndroidInjectActivityModule::class],
    autoInclude = false,
    flatten = true
)
interface ActivityInjectorComponent : HasActivityInjector

@BootstrapComponent(
    bootstrapDependencies = [ViewModelComponent::class],
    applicationModules = [AndroidInjectFragmentModule::class],
    autoInclude = false,
    flatten = true
)
interface FragmentInjectorComponent : HasFragmentInjector

typealias SupportFragment = android.support.v4.app.Fragment

@BootstrapComponent(
    bootstrapDependencies = [ViewModelComponent::class],
    applicationModules = [AndroidInjectSupportFragmentModule::class],
    autoInclude = false,
    flatten = true
)
interface SupportFragmentInjectorComponent : HasSupportFragmentInjector

@BootstrapComponent(
    applicationModules = [AndroidInjectServiceModule::class],
    autoInclude = false,
    flatten = true
)
interface ServiceInjectorComponent : HasServiceInjector

@BootstrapComponent(
    applicationModules = [AndroidInjectBroadcastReceiverModule::class],
    autoInclude = false,
    flatten = true
)
interface BroadcastReceiverInjectorComponent : HasBroadcastReceiverInjector

@BootstrapComponent(
    applicationModules = [AndroidInjectContentProviderModule::class],
    autoInclude = false,
    flatten = true
)
interface ContentProviderInjectorComponent : HasContentProviderInjector

