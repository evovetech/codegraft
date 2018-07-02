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

@file:JvmName("AndroidInjections")

package sourcerer.inject.android

import android.app.Activity
import android.app.Application
import android.app.Fragment
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ContentProvider
import android.content.Context
import android.util.Log
import android.util.Log.DEBUG
import dagger.android.AndroidInjector

private val TAG = "sourcerer.inject.android"

/**
 * Injects `activity` if an associated [AndroidInjector] implementation can be found,
 * otherwise throws an [IllegalArgumentException].
 *
 * @throws RuntimeException if the [Application] doesn't implement [                          ].
 */
fun Activity.inject() {
    val application = application
    if (application !is HasActivityInjector) {
        throw RuntimeException(
            String.format(
                "%s does not implement %s",
                application.javaClass.canonicalName,
                HasActivityInjector::class.java.canonicalName
            )
        )
    }
    application.activityInjector.inject(this)
}

/**
 * Injects `fragment` if an associated [AndroidInjector] implementation can be found,
 * otherwise throws an [IllegalArgumentException].
 *
 *
 * Uses the following algorithm to find the appropriate `AndroidInjector<Fragment>` to
 * use to inject `fragment`:
 *
 *
 *  1. Walks the parent-fragment hierarchy to find the a fragment that implements [ ], and if none do
 *  1. Uses the `fragment`'s [activity][Fragment.getActivity] if it implements
 * [HasFragmentInjector], and if not
 *  1. Uses the [android.app.Application] if it implements [HasFragmentInjector].
 *
 *
 *
 * If none of them implement [HasFragmentInjector], a [IllegalArgumentException] is
 * thrown.
 *
 * @throws IllegalArgumentException if no parent fragment, activity, or application implements
 * [HasFragmentInjector].
 */
fun Fragment.inject() {
    val hasFragmentInjector = findHasFragmentInjector()
    if (Log.isLoggable(TAG, DEBUG)) {
        Log.d(
            TAG,
            String.format(
                "An injector for %s was found in %s",
                javaClass.canonicalName,
                hasFragmentInjector.javaClass.canonicalName
            )
        )
    }
    hasFragmentInjector.fragmentInjector.inject(this)
}

fun android.support.v4.app.Fragment.inject() {
    val hasSupportFragmentInjector = findHasSupportFragmentInjector()
    if (Log.isLoggable(TAG, DEBUG)) {
        Log.d(
            TAG,
            String.format(
                "An injector for %s was found in %s",
                javaClass.canonicalName,
                hasSupportFragmentInjector.javaClass.canonicalName
            )
        )
    }
    hasSupportFragmentInjector.supportFragmentInjector.inject(this)
}

/**
 * Injects `service` if an associated [AndroidInjector] implementation can be found,
 * otherwise throws an [IllegalArgumentException].
 *
 * @throws RuntimeException if the [Application] doesn't implement [                          ].
 */
fun Service.inject() {
    val application = application
    if (application !is HasServiceInjector) {
        throw RuntimeException(
            String.format(
                "%s does not implement %s",
                application.javaClass.canonicalName,
                HasServiceInjector::class.java.canonicalName
            )
        )
    }
    application.serviceInjector.inject(this)
}

/**
 * Injects `broadcastReceiver` if an associated [AndroidInjector] implementation can
 * be found, otherwise throws an [IllegalArgumentException].
 *
 * @throws RuntimeException if the [Application] from [                          ][Context.getApplicationContext] doesn't implement [HasBroadcastReceiverInjector].
 */
fun BroadcastReceiver.inject(context: Context) {
    val application = context.applicationContext as Application
    if (application !is HasBroadcastReceiverInjector) {
        throw RuntimeException(
            String.format(
                "%s does not implement %s",
                application.javaClass.canonicalName,
                HasBroadcastReceiverInjector::class.java.canonicalName
            )
        )
    }
    application.broadcastReceiverInjector.inject(this)
}

/**
 * Injects `contentProvider` if an associated [AndroidInjector] implementation can be
 * found, otherwise throws an [IllegalArgumentException].
 *
 * @throws RuntimeException if the [Application] doesn't implement [                          ].
 */
fun ContentProvider.inject() {
    val application = context.applicationContext as Application
    if (application !is HasContentProviderInjector) {
        throw RuntimeException(
            String.format(
                "%s does not implement %s",
                application.javaClass.canonicalName,
                HasContentProviderInjector::class.java.canonicalName
            )
        )
    }
    application.contentProviderInjector.inject(this)
}

private
fun Fragment.findHasFragmentInjector(): HasFragmentInjector {
    // TODO:
    //        Fragment parentFragment = fragment;
    //        while ((parentFragment = parentFragment.getParentFragment()) != null) {
    //            if (parentFragment instanceof HasFragmentInjector) {
    //                return (HasFragmentInjector) parentFragment;
    //            }
    //        }
    val activity = activity
    when (activity) {
        is HasFragmentInjector -> return activity
    }
    val application = activity.application
    when (application) {
        is HasFragmentInjector -> return application
    }
    throw IllegalArgumentException(
        String.format("No injector was found for %s", javaClass.canonicalName)
    )
}

private
fun android.support.v4.app.Fragment.findHasSupportFragmentInjector(): HasSupportFragmentInjector {
    // TODO:
    //        android.support.v4.app.Fragment parentFragment = fragment;
    //        while ((parentFragment = parentFragment.getParentFragment()) != null) {
    //            if (parentFragment instanceof HasSupportFragmentInjector) {
    //                return (HasSupportFragmentInjector) parentFragment;
    //            }
    //        }
    val activity = activity
    when (activity) {
        is HasSupportFragmentInjector -> return activity
    }
    val application = activity.application
    when (application) {
        is HasSupportFragmentInjector -> return application
    }
    throw IllegalArgumentException(
        String.format("No injector was found for %s", javaClass.canonicalName)
    )
}
