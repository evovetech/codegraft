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
