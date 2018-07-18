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
import codegraft.inject.android.BootApplication;
import codegraft.inject.android.HasApplicationInjector;
import dagger.android.AndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasBroadcastReceiverInjector;
import dagger.android.HasContentProviderInjector;
import dagger.android.HasFragmentInjector;
import dagger.android.HasServiceInjector;
import dagger.android.support.HasSupportFragmentInjector;
import net.bytebuddy.implementation.bind.annotation.This;
import org.jetbrains.annotations.NotNull;

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
    AndroidInjector<Activity> activityInjector(@This BootApplication<?> application) {
        Object component = application.getBootstrap().getComponent();
        if (component instanceof HasActivityInjector) {
            return ((HasActivityInjector) component).activityInjector();
        }
        throw new IllegalStateException(component + " does not implement " + HasActivityInjector.class.getCanonicalName());
    }

    @NotNull
    public static
    AndroidInjector<Service> serviceInjector(@This BootApplication<?> application) {
        Object component = application.getBootstrap().getComponent();
        if (component instanceof HasServiceInjector) {
            return ((HasServiceInjector) component).serviceInjector();
        }
        throw new IllegalStateException(component + " does not implement " + HasServiceInjector.class.getCanonicalName());
    }

    @NotNull
    public static
    AndroidInjector<BroadcastReceiver> broadcastReceiverInjector(@This BootApplication<?> application) {
        Object component = application.getBootstrap().getComponent();
        if (component instanceof HasBroadcastReceiverInjector) {
            return ((HasBroadcastReceiverInjector) component).broadcastReceiverInjector();
        }
        throw new IllegalStateException(component + " does not implement " + HasBroadcastReceiverInjector.class.getCanonicalName());
    }

    @NotNull
    public static
    AndroidInjector<ContentProvider> contentProviderInjector(@This BootApplication<?> application) {
        Object component = application.getBootstrap().getComponent();
        if (component instanceof HasContentProviderInjector) {
            return ((HasContentProviderInjector) component).contentProviderInjector();
        }
        throw new IllegalStateException(component + " does not implement " + HasContentProviderInjector.class.getCanonicalName());
    }

    @NotNull
    public static
    AndroidInjector<Fragment> fragmentInjector(@This BootApplication<?> application) {
        Object component = application.getBootstrap().getComponent();
        if (component instanceof HasFragmentInjector) {
            return ((HasFragmentInjector) component).fragmentInjector();
        }
        throw new IllegalStateException(component + " does not implement " + HasFragmentInjector.class.getCanonicalName());
    }

    @NotNull
    public static
    AndroidInjector<android.support.v4.app.Fragment> supportFragmentInjector(@This BootApplication<?> application) {
        Object component = application.getBootstrap().getComponent();
        if (component instanceof HasSupportFragmentInjector) {
            return ((HasSupportFragmentInjector) component).supportFragmentInjector();
        }
        throw new IllegalStateException(component + " does not implement " + HasSupportFragmentInjector.class.getCanonicalName());
    }
}
