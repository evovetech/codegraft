/*
 * Copyright 2016 Layne Mobile, LLC
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

package com.laynemobile.proxy;

import com.laynemobile.proxy.internal.ProxyLog;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import static java.lang.String.format;
import static java.util.Locale.US;

abstract class AbstractInvocationHandler implements InvocationHandler, MethodHandler {
    private static final String TAG = AbstractInvocationHandler.class.getSimpleName();
    private static final ThreadLocal<MethodResult> LOCAL_METHOD_RESULT = new ThreadLocal<MethodResult>() {
        @Override protected MethodResult initialValue() {
            return new MethodResult();
        }
    };

    @Override public final Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        ProxyLog.d(TAG, "calling method: %s", method);
        MethodResult result = LOCAL_METHOD_RESULT.get();
        result.set(null);
        if (handle(proxy, method, args, result)) {
            Object r = result.get();
            ProxyLog.d(TAG, "handled method: %s, result: %s", method, r);
            return r;
        }
        return callObjectMethod(proxy, method, args);
    }

    private static boolean isObjectMethod(Method m) {
        switch (m.getName()) {
            case "toString":
                return (m.getReturnType() == String.class
                        && m.getParameterTypes().length == 0);
            case "hashCode":
                return (m.getReturnType() == int.class
                        && m.getParameterTypes().length == 0);
            case "equals":
                return (m.getReturnType() == boolean.class
                        && m.getParameterTypes().length == 1
                        && m.getParameterTypes()[0] == Object.class);
        }
        return false;
    }

    private static Object callObjectMethod(Object self, Method m, Object[] args) {
        if (!isObjectMethod(m)) {
            String format = "%s: unable to invoke method '%s'";
            throw new IllegalStateException(format(US, format, self, m));
        }
        switch (m.getName()) {
            case "toString":
                return self.getClass().getName() + "@" + Integer.toHexString(self.hashCode());
            case "hashCode":
                return System.identityHashCode(self);
            case "equals":
                return (self == args[0]);
        }
        return null;
    }
}
