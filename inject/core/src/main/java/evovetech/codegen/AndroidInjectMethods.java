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
import android.app.Fragment;
import android.app.Service;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;

import static sourcerer.inject.android.AndroidInjections.inject;

/**
 * Injects core Android types.
 */
public final
class AndroidInjectMethods {
    private
    AndroidInjectMethods() {}

    public static
    void onCreate(
            @This Activity activity,
            @SuperCall Runnable super$call
    ) {
        inject(activity);
        super$call.run();
    }

    public static
    void onCreate(
            @This Fragment fragment,
            @SuperCall Runnable super$call
    ) {
        inject(fragment);
        super$call.run();
    }

    public static
    void onCreate(
            @This android.support.v4.app.Fragment fragment,
            @SuperCall Runnable super$call
    ) {
        inject(fragment);
        super$call.run();
    }

    public static
    void onCreate(
            @This Service service,
            @SuperCall Runnable super$call
    ) {
        inject(service);
        super$call.run();
    }
}
