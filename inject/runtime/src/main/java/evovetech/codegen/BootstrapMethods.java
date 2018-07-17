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

package evovetech.codegen;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import dagger.android.AndroidInjector;
import net.bytebuddy.implementation.bind.annotation.This;
import org.jetbrains.annotations.NotNull;
import codegraft.inject.android.BootApplication;
import codegraft.inject.android.HasActivityInjector;
import codegraft.inject.android.HasApplicationInjector;
import codegraft.inject.android.HasBroadcastReceiverInjector;
import codegraft.inject.android.HasContentProviderInjector;
import codegraft.inject.android.HasFragmentInjector;
import codegraft.inject.android.HasServiceInjector;
import codegraft.inject.android.HasSupportFragmentInjector;

public final
class BootstrapMethods {
    private
    BootstrapMethods() { throw new AssertionError("no instances"); }

    @NotNull
    public static
    AndroidInjector<Application> getApplicationInjector(@This BootApplication<?> application) {
        Object component = application.getBootstrap().getComponent();
        if (component instanceof HasApplicationInjector) {
            return ((HasApplicationInjector) component).getApplicationInjector();
        }
        throw new IllegalStateException(component + " does not implement " + HasApplicationInjector.class.getCanonicalName());
    }

    @NotNull
    public static
    AndroidInjector<Activity> getActivityInjector(@This BootApplication<?> application) {
        Object component = application.getBootstrap().getComponent();
        if (component instanceof HasActivityInjector) {
            return ((HasActivityInjector) component).getActivityInjector();
        }
        throw new IllegalStateException(component + " does not implement " + HasActivityInjector.class.getCanonicalName());
    }

    @NotNull
    public static
    AndroidInjector<Service> getServiceInjector(@This BootApplication<?> application) {
        Object component = application.getBootstrap().getComponent();
        if (component instanceof HasServiceInjector) {
            return ((HasServiceInjector) component).getServiceInjector();
        }
        throw new IllegalStateException(component + " does not implement " + HasServiceInjector.class.getCanonicalName());
    }

    @NotNull
    public static
    AndroidInjector<BroadcastReceiver> getBroadcastReceiverInjector(@This BootApplication<?> application) {
        Object component = application.getBootstrap().getComponent();
        if (component instanceof HasBroadcastReceiverInjector) {
            return ((HasBroadcastReceiverInjector) component).getBroadcastReceiverInjector();
        }
        throw new IllegalStateException(component + " does not implement " + HasBroadcastReceiverInjector.class.getCanonicalName());
    }

    @NotNull
    public static
    AndroidInjector<ContentProvider> getContentProviderInjector(@This BootApplication<?> application) {
        Object component = application.getBootstrap().getComponent();
        if (component instanceof HasContentProviderInjector) {
            return ((HasContentProviderInjector) component).getContentProviderInjector();
        }
        throw new IllegalStateException(component + " does not implement " + HasContentProviderInjector.class.getCanonicalName());
    }

    @NotNull
    public static
    AndroidInjector<Fragment> getFragmentInjector(@This BootApplication<?> application) {
        Object component = application.getBootstrap().getComponent();
        if (component instanceof HasFragmentInjector) {
            return ((HasFragmentInjector) component).getFragmentInjector();
        }
        throw new IllegalStateException(component + " does not implement " + HasFragmentInjector.class.getCanonicalName());
    }

    @NotNull
    public static
    AndroidInjector<android.support.v4.app.Fragment> getSupportFragmentInjector(@This BootApplication<?> application) {
        Object component = application.getBootstrap().getComponent();
        if (component instanceof HasSupportFragmentInjector) {
            return ((HasSupportFragmentInjector) component).getSupportFragmentInjector();
        }
        throw new IllegalStateException(component + " does not implement " + HasSupportFragmentInjector.class.getCanonicalName());
    }
}
