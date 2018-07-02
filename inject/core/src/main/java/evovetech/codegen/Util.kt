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

@file:JvmName("Util")

package evovetech.codegen

import android.app.Fragment
import sourcerer.inject.android.HasFragmentInjector
import sourcerer.inject.android.HasSupportFragmentInjector

internal
fun findHasFragmentInjector(
    fragment: Fragment
): HasFragmentInjector {
    // TODO:
    //        Fragment parentFragment = fragment;
    //        while ((parentFragment = parentFragment.getParentFragment()) != null) {
    //            if (parentFragment instanceof HasFragmentInjector) {
    //                return (HasFragmentInjector) parentFragment;
    //            }
    //        }
    val activity = fragment.activity
    when (activity) {
        is HasFragmentInjector -> return activity
    }
    val application = activity.application
    when (application) {
        is HasFragmentInjector -> return application
    }
    throw IllegalArgumentException(
        String.format("No injector was found for %s", fragment.javaClass.canonicalName)
    )
}

internal
fun findHasSupportFragmentInjector(
    fragment: android.support.v4.app.Fragment
): HasSupportFragmentInjector {
    // TODO:
    //        android.support.v4.app.Fragment parentFragment = fragment;
    //        while ((parentFragment = parentFragment.getParentFragment()) != null) {
    //            if (parentFragment instanceof HasSupportFragmentInjector) {
    //                return (HasSupportFragmentInjector) parentFragment;
    //            }
    //        }
    val activity = fragment.activity
    when (activity) {
        is HasSupportFragmentInjector -> return activity
    }
    val application = activity.application
    when (application) {
        is HasSupportFragmentInjector -> return application
    }
    throw IllegalArgumentException(
        String.format("No injector was found for %s", fragment.javaClass.canonicalName)
    )
}
