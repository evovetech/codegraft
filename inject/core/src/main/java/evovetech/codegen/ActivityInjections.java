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
import dagger.android.AndroidInjector;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.Advice.This;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import sourcerer.inject.android.HasActivityInjector;

/**
 * Injects core Android types.
 */
public
class ActivityInjections<A extends Activity> {
    private static final String TAG = "evovetech.codegen";

//    public static <A extends Activity> ActivityInjections<A> asType(TypeDescription typeDescription, ByteBuddy byteBuddy) {
//        byteBuddy.subclass(typeDescription)
//                .transform()
//    }

    /**
     * Injects {@code activity} if an associated {@link AndroidInjector} implementation can be found,
     * otherwise throws an {@link IllegalArgumentException}.
     *
     * @throws RuntimeException if the {@link Application} doesn't implement {@link
     *                          HasActivityInjector}.
     */
    public
    void onCreate(
            @This @RuntimeType A activity,
            @SuperCall Runnable super$call,
            @AllArguments Object[] arguments
    ) {
        Application application = activity.getApplication();
        if (!(application instanceof HasActivityInjector)) {
            throw new RuntimeException(
                    String.format(
                            "%s does not implement %s",
                            application.getClass().getCanonicalName(),
                            HasActivityInjector.class.getCanonicalName()
                    ));
        }

        AndroidInjector<Activity> activityInjector =
                ((HasActivityInjector) application).getActivityInjector();
        activityInjector.inject(activity);

        // call super
        super$call.run();
//        super$onCreate.run();
    }
//
//    /**
//     * Injects {@code fragment} if an associated {@link AndroidInjector} implementation can be found,
//     * otherwise throws an {@link IllegalArgumentException}.
//     *
//     * <p>Uses the following algorithm to find the appropriate {@code AndroidInjector<Fragment>} to
//     * use to inject {@code fragment}:
//     *
//     * <ol>
//     * <li>Walks the parent-fragment hierarchy to find the a fragment that implements {@link
//     * HasFragmentInjector}, and if none do
//     * <li>Uses the {@code fragment}'s {@link Fragment#getActivity() activity} if it implements
//     * {@link HasFragmentInjector}, and if not
//     * <li>Uses the {@link android.app.Application} if it implements {@link HasFragmentInjector}.
//     * </ol>
//     * <p>
//     * If none of them implement {@link HasFragmentInjector}, a {@link IllegalArgumentException} is
//     * thrown.
//     *
//     * @throws IllegalArgumentException if no parent fragment, activity, or application implements
//     *                                  {@link HasFragmentInjector}.
//     */
//    public static
//    <T extends Fragment>
//    void onAttach(@This T fragment, @SuperCall Runnable super$onCreate) {
//        HasFragmentInjector hasFragmentInjector = findHasFragmentInjector(fragment);
//        if (Log.isLoggable(TAG, DEBUG)) {
//            Log.d(
//                    TAG,
//                    String.format(
//                            "An injector for %s was found in %s",
//                            fragment.getClass().getCanonicalName(),
//                            hasFragmentInjector.getClass().getCanonicalName()
//                    )
//            );
//        }
//
//        AndroidInjector<Fragment> fragmentInjector = hasFragmentInjector.getFragmentInjector();
//        fragmentInjector.inject(fragment);
//
//        // call super
//        super$onCreate.run();
//    }
//    public static
//    <T extends android.support.v4.app.Fragment>
//    void inject(@This T fragment, @SuperCall Runnable super$call) {
//        HasSupportFragmentInjector hasSupportFragmentInjector = findHasSupportFragmentInjector(fragment);
//        if (Log.isLoggable(TAG, DEBUG)) {
//            Log.d(
//                    TAG,
//                    String.format(
//                            "An injector for %s was found in %s",
//                            fragment.getClass().getCanonicalName(),
//                            hasSupportFragmentInjector.getClass().getCanonicalName()
//                    )
//            );
//        }
//
//        AndroidInjector<android.support.v4.app.Fragment> supportFragmentInjector =
//                hasSupportFragmentInjector.getSupportFragmentInjector();
//        supportFragmentInjector.inject(fragment);
//    }
//
//    /**
//     * Injects {@code service} if an associated {@link AndroidInjector} implementation can be found,
//     * otherwise throws an {@link IllegalArgumentException}.
//     *
//     * @throws RuntimeException if the {@link Application} doesn't implement {@link
//     *                          HasServiceInjector}.
//     */
//    public static
//    <T extends Service>
//    void onCreate(@This T service, @SuperCall Runnable super$onCreate) {
//        Application application = service.getApplication();
//        if (!(application instanceof HasServiceInjector)) {
//            throw new RuntimeException(
//                    String.format(
//                            "%s does not implement %s",
//                            application.getClass().getCanonicalName(),
//                            HasServiceInjector.class.getCanonicalName()
//                    ));
//        }
//
//        AndroidInjector<Service> serviceInjector = ((HasServiceInjector) application).getServiceInjector();
//        serviceInjector.inject(service);
//
//        super$onCreate.run();
//    }
//
//    // TODO:
//    /**
//     * Injects {@code broadcastReceiver} if an associated {@link AndroidInjector} implementation can
//     * be found, otherwise throws an {@link IllegalArgumentException}.
//     *
//     * @throws RuntimeException if the {@link Application} from {@link
//     *                          Context#getApplicationContext()} doesn't implement {@link HasBroadcastReceiverInjector}.
//     */
//    public static
//    <T extends BroadcastReceiver>
//    void inject(
//            @This T broadcastReceiver,
//            @SuperCall Runnable super$call,
//            Context context
//    ) {
//        Application application = (Application) context.getApplicationContext();
//        if (!(application instanceof HasBroadcastReceiverInjector)) {
//            throw new RuntimeException(
//                    String.format(
//                            "%s does not implement %s",
//                            application.getClass().getCanonicalName(),
//                            HasBroadcastReceiverInjector.class.getCanonicalName()
//                    ));
//        }
//
//        AndroidInjector<BroadcastReceiver> broadcastReceiverInjector =
//                ((HasBroadcastReceiverInjector) application).getBroadcastReceiverInjector();
//        broadcastReceiverInjector.inject(broadcastReceiver);
//    }
//
//    /**
//     * Injects {@code contentProvider} if an associated {@link AndroidInjector} implementation can be
//     * found, otherwise throws an {@link IllegalArgumentException}.
//     *
//     * @throws RuntimeException if the {@link Application} doesn't implement {@link
//     *                          HasContentProviderInjector}.
//     */
//    public static
//    <T extends ContentProvider>
//    void onCreate(
//            @This T contentProvider,
//            @SuperCall Runnable super$onCreate
//    ) {
//        Application application = (Application) contentProvider.getContext().getApplicationContext();
//        if (!(application instanceof HasContentProviderInjector)) {
//            throw new RuntimeException(
//                    String.format(
//                            "%s does not implement %s",
//                            application.getClass().getCanonicalName(),
//                            HasContentProviderInjector.class.getCanonicalName()
//                    ));
//        }
//
//        AndroidInjector<ContentProvider> contentProviderInjector =
//                ((HasContentProviderInjector) application).getContentProviderInjector();
//        contentProviderInjector.inject(contentProvider);
//
//        // call super
//        super$onCreate.run();
//    }
}
