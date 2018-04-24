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

import com.laynemobile.proxy.MethodResult;
import com.laynemobile.proxy.NamedMethodHandler;
import com.laynemobile.proxy.TypeToken;
import com.laynemobile.proxy.functions.transforms.ProxyFunctionTransform;
import com.laynemobile.proxy.internal.ProxyLog;

import java.lang.reflect.Method;
import java.util.List;

public class ProxyFunction2<P, F extends ProxyFunctionTransform<P, ?, R>, R>
        // TODO: implement InvocationHandler instead of this
        implements NamedMethodHandler {
    private static final String TAG = ProxyFunction2.class.getSimpleName();

    private final ProxyFunctionDef<P, F, R> functionDef;
    private final F function;

    public ProxyFunction2(ProxyFunctionDef<P, F, R> functionDef, F function) {
        this.functionDef = functionDef;
        this.function = function;
    }

    @Override public final String name() {
        return functionDef.name();
    }

    public final ProxyFunctionDef<P, F, R> functionDef() {
        return functionDef;
    }

    public final F function() {
        return function;
    }

    @Override
    public final boolean handle(Object proxy, Method method, Object[] args, MethodResult result) throws Throwable {
        @SuppressWarnings("unchecked")
        P p = (P) proxy;
        List<TypeToken<?>> handlerParamTypes = functionDef.paramTypes();
        int length = handlerParamTypes.size();
        Class<?>[] paramTypes = method.getParameterTypes();
//        ProxyLog.v(TAG, "method parameterTypes: %s", Arrays.toString(paramTypes));
        if (length != paramTypes.length) {
            return false;
        } else if (length == 0) {
            result.set(function().call(p));
            return true;
        }

        Class<?> handlerReturnType = functionDef.returnType().getRawType();
        Class<?> returnType = method.getReturnType();
        if (!handlerReturnType.isAssignableFrom(returnType)) {
            ProxyLog.w(TAG, "return type '%s' not instance of handler return type '%s'", returnType, handlerReturnType);
            return false;
        }
//        ProxyLog.v(TAG, "return type '%s' instance of handler return type '%s'", returnType, handlerReturnType);

        for (int i = 0; i < length; i++) {
            Class<?> handlerParamType = handlerParamTypes.get(i).getRawType();
            Class<?> paramType = paramTypes[i];
            if (!handlerParamType.isAssignableFrom(paramType)) {
                ProxyLog.w(TAG, "param type '%s' not instance of handler type '%s'", paramType, handlerParamType);
                return false;
            }
//            ProxyLog.v(TAG, "param type '%s' instance of handler type '%s'", paramType, handlerParamType);
        }

        result.set(function().call(p, args));
        return true;
    }
}
