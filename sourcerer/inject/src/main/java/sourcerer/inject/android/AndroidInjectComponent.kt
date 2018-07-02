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
import dagger.android.AndroidInjector
import sourcerer.inject.BootstrapComponent

@BootstrapComponent(
    applicationModules = [AndroidInjectApplicationModule::class],
    autoInclude = true,
    flatten = true
)
interface HasApplicationInjector {
    val applicationInjector: AndroidInjector<AndroidApplication>
}

@BootstrapComponent(
    applicationModules = [AndroidInjectActivityModule::class],
    // TODO
    autoInclude = true,
    flatten = true
)
interface HasActivityInjector {
    val activityInjector: AndroidInjector<Activity>
}

@BootstrapComponent(
    applicationModules = [AndroidInjectFragmentModule::class],
    autoInclude = false,
    flatten = true
)
interface HasFragmentInjector {
    val fragmentInjector: AndroidInjector<Fragment>
}

typealias SupportFragment = android.support.v4.app.Fragment

@BootstrapComponent(
    applicationModules = [AndroidInjectSupportFragmentModule::class],
    autoInclude = false,
    flatten = true
)
interface HasSupportFragmentInjector {
    val supportFragmentInjector: AndroidInjector<SupportFragment>
}

@BootstrapComponent(
    applicationModules = [AndroidInjectServiceModule::class],
    // TODO
    autoInclude = true,
    flatten = true
)
interface HasServiceInjector {
    val serviceInjector: AndroidInjector<Service>
}

@BootstrapComponent(
    applicationModules = [AndroidInjectBroadcastReceiverModule::class],
    // TODO
    autoInclude = true,
    flatten = true
)
interface HasBroadcastReceiverInjector {
    val broadcastReceiverInjector: AndroidInjector<BroadcastReceiver>
}

@BootstrapComponent(
    applicationModules = [AndroidInjectContentProviderModule::class],
    // TODO
    autoInclude = true,
    flatten = true
)
interface HasContentProviderInjector {
    val contentProviderInjector: AndroidInjector<ContentProvider>
}

