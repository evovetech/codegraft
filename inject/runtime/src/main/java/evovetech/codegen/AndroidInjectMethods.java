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

package evovetech.codegen;

import android.app.Activity;
import android.app.Fragment;
import android.app.Service;
import dagger.android.support.AndroidSupportInjection;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;

import static dagger.android.AndroidInjection.inject;

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
    void onAttach(
            @This Fragment fragment,
            @SuperCall Runnable super$call
    ) {
        inject(fragment);
        super$call.run();
    }

    public static
    void onAttach(
            @This android.support.v4.app.Fragment fragment,
            @SuperCall Runnable super$call
    ) {
        AndroidSupportInjection.inject(fragment);
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
