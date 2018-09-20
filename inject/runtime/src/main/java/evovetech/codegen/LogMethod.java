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

import android.text.TextUtils;
import android.util.Log;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.SuperMethod;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final
class LogMethod {
    public static
    void log(
            @SuperCall Runnable super$call,
            @SuperMethod Method super$method,
            @AllArguments Object[] arguments
    ) {
        final String TAG = super$method.getDeclaringClass().getCanonicalName();
        final String methodName = super$method.getName();
        final String name;
        final int index = methodName.indexOf("$");
        if (index == -1) {
            name = methodName;
        } else {
            name = methodName.substring(0, index);
        }
        final String args;
        if (arguments.length == 0) {
            args = "";
        } else {
            final List<String> params = new ArrayList<>();
            final Class<?>[] paramTypes = super$method.getParameterTypes();
            for (int i = 0; i < paramTypes.length; i++) {
                String param = String.format("%s = %s", paramTypes[i].getCanonicalName(), arguments[i]);
                params.add(i, param);
            }
            args = TextUtils.join(", ", params);
        }
        final String msg = String.format("%s(%s)", name, args);
        Log.d(TAG, String.format("before calling %s", msg));
        try {
            super$call.run();
        } finally {
            Log.d(TAG, String.format("after calling %s", msg));
        }
    }
}
