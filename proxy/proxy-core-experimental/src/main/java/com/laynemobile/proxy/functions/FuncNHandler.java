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

package com.laynemobile.proxy.functions;

import com.laynemobile.proxy.MethodHandler;
import com.laynemobile.proxy.MethodResult;
import com.laynemobile.proxy.TypeToken;
import com.laynemobile.proxy.internal.ProxyLog;

import java.lang.reflect.Method;
import java.util.Arrays;

public class FuncNHandler implements MethodHandler {
    private static final String TAG = FuncNHandler.class.getSimpleName();

    private final FuncN<?> funcN;
    private final TypeToken<?>[] paramTypes;
    private final int length;

    public FuncNHandler(FuncN<?> funcN, TypeToken<?>[] paramTypes) {
        this.funcN = funcN;
        this.paramTypes = paramTypes;
        this.length = paramTypes.length;
    }

    @Override
    public final boolean handle(Object proxy, Method method, Object[] args, MethodResult result) throws Throwable {
        Class<?>[] parameterTypes = method.getParameterTypes();
        ProxyLog.d(TAG, "method parameterTypes: %s", Arrays.toString(parameterTypes));
        if (length != parameterTypes.length) {
            return false;
        }

        for (int i = 0; i < length; i++) {
            TypeToken<?> type = paramTypes[i];
            Class<?> clazz = parameterTypes[i];
            if (!clazz.isAssignableFrom(type.getRawType())) {
                ProxyLog.w(TAG, "param type '%s' not assignable from handler type '%s'", clazz, type.getRawType());
                return false;
            }
        }

        result.set(funcN.call(args));
        return true;
    }
}
