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

import android.app.Activity
import android.app.Fragment
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ContentProvider
import dagger.android.AndroidInjector

interface HasApplicationInjector {
    val applicationInjector: AndroidInjector<AndroidApplication>
}

interface HasActivityInjector : dagger.android.HasActivityInjector {
    val activityInjector: AndroidInjector<Activity>

    override
    fun activityInjector(): AndroidInjector<Activity> {
        return activityInjector
    }
}

interface HasFragmentInjector : dagger.android.HasFragmentInjector {
    val fragmentInjector: AndroidInjector<Fragment>

    override
    fun fragmentInjector(): AndroidInjector<Fragment> {
        return fragmentInjector
    }
}

interface HasSupportFragmentInjector : dagger.android.support.HasSupportFragmentInjector {
    val supportFragmentInjector: AndroidInjector<SupportFragment>

    override
    fun supportFragmentInjector(): AndroidInjector<android.support.v4.app.Fragment> {
        return supportFragmentInjector
    }
}

interface HasServiceInjector : dagger.android.HasServiceInjector {
    val serviceInjector: AndroidInjector<Service>

    override
    fun serviceInjector(): AndroidInjector<Service> {
        return serviceInjector
    }
}

interface HasBroadcastReceiverInjector : dagger.android.HasBroadcastReceiverInjector {
    val broadcastReceiverInjector: AndroidInjector<BroadcastReceiver>

    override
    fun broadcastReceiverInjector(): AndroidInjector<BroadcastReceiver> {
        return broadcastReceiverInjector
    }
}

interface HasContentProviderInjector : dagger.android.HasContentProviderInjector {
    val contentProviderInjector: AndroidInjector<ContentProvider>

    override
    fun contentProviderInjector(): AndroidInjector<ContentProvider> {
        return contentProviderInjector
    }
}
