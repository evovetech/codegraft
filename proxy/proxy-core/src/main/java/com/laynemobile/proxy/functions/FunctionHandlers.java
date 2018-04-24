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
import com.laynemobile.proxy.internal.ProxyLog;

import java.lang.reflect.Method;
import java.util.Arrays;

public final class FunctionHandlers {
    private FunctionHandlers() { throw new AssertionError("no instances"); }

    public static <R> MethodHandler from(Func0<R> func0) {
        return new AbstractFunctionHandler<Func0<R>>(func0) {
            @Override protected boolean tryHandle(Object proxy, Method method, Class<?>[] paramTypes,
                    Object[] args, MethodResult result) throws Throwable {
                if (paramTypes.length == 0) {
                    result.set(function.call());
                    return true;
                }
                return false;
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static <T, R> MethodHandler from(Func1<T, R> func1) {
        return new AbstractFunctionHandler<Func1<T, R>>(func1) {
            @Override protected boolean tryHandle(Object proxy, Method method, Class<?>[] paramTypes,
                    Object[] args, MethodResult result) throws Throwable {
                if (paramTypes.length == 1) {
                    result.set(function.call((T) args[0]));
                    return true;
                }
                return false;
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static <T1, T2, R> MethodHandler from(Func2<T1, T2, R> func2) {
        return new AbstractFunctionHandler<Func2<T1, T2, R>>(func2) {
            @Override protected boolean tryHandle(Object proxy, Method method, Class<?>[] paramTypes,
                    Object[] args, MethodResult result) throws Throwable {
                if (paramTypes.length == 2) {
                    result.set(function.call((T1) args[0], (T2) args[1]));
                    return true;
                }
                return false;
            }
        };
    }

    public static MethodHandler from(Action0 action0) {
        return new AbstractFunctionHandler<Action0>(action0) {
            @Override protected boolean tryHandle(Object proxy, Method method, Class<?>[] paramTypes, Object[] args,
                    MethodResult result) throws Throwable {
                if (paramTypes.length == 0) {
                    function.call();
                    return true;
                }
                return false;
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static <T> MethodHandler from(Action1<T> action1) {
        return new AbstractFunctionHandler<Action1<T>>(action1) {
            @Override protected boolean tryHandle(Object proxy, Method method, Class<?>[] paramTypes, Object[] args,
                    MethodResult result) throws Throwable {
                if (paramTypes.length == 1) {
                    function.call((T) args[0]);
                    return true;
                }
                return false;
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static <T1, T2> MethodHandler from(Action2<T1, T2> action2) {
        return new AbstractFunctionHandler<Action2<T1, T2>>(action2) {
            @Override protected boolean tryHandle(Object proxy, Method method, Class<?>[] paramTypes, Object[] args,
                    MethodResult result) throws Throwable {
                if (paramTypes.length == 2) {
                    function.call((T1) args[0], (T2) args[1]);
                    return true;
                }
                return false;
            }
        };
    }

    private static abstract class AbstractFunctionHandler<F extends Function> implements MethodHandler {
        private static final String TAG = AbstractFunctionHandler.class.getSimpleName();

        protected final F function;

        protected AbstractFunctionHandler(F function) {
            this.function = function;
        }

        protected abstract boolean tryHandle(Object proxy, Method method, Class<?>[] paramTypes,
                Object[] args, MethodResult result) throws Throwable;

        @Override
        public final boolean handle(Object proxy, Method method, Object[] args, MethodResult result) throws Throwable {
            try {
                Class<?>[] parameterTypes = method.getParameterTypes();
                ProxyLog.d(TAG, "method parameterTypes: %s", Arrays.toString(parameterTypes));
                return tryHandle(proxy, method, parameterTypes, args, result);
            } catch (ClassCastException e) {
                ProxyLog.e("FunctionHandlers", "error handling", e);
                return false;
            }
        }
    }
}
