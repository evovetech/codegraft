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
