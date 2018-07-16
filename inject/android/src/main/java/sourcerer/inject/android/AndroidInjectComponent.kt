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

import sourcerer.inject.BootstrapComponent

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

