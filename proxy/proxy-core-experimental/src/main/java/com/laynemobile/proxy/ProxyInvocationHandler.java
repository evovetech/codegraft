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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class ProxyInvocationHandler<T> extends AbstractInvocationHandler {
    private static final String TAG = ProxyInvocationHandler.class.getSimpleName();

    private final TypeToken<T> type;
    private final ConcurrentHashMap<String, MethodHandler> handlers;

    ProxyInvocationHandler(TypeToken<T> type, Map<String, ? extends List<? extends NamedMethodHandler>> _handlers) {
        ConcurrentHashMap<String, MethodHandler> handlers = new ConcurrentHashMap<>(_handlers.size(), 0.75f, 1);
        for (Map.Entry<String, ? extends List<? extends NamedMethodHandler>> entry : _handlers.entrySet()) {
            handlers.put(entry.getKey(), MethodHandlers.create(entry.getValue()));
        }
        this.type = type;
        this.handlers = handlers;
    }

    @Override
    public boolean handle(Object proxy, Method method, Object[] args, MethodResult result) throws Throwable {
        return get(method).handle(proxy, method, args, result)
                || handleProxyObjectMethod(proxy, method, result);
    }

    private MethodHandler get(Method method) {
        String name = method.getName();
        MethodHandler handler = handlers.get(name);
        if (handler == null) {
            synchronized (handlers) {
                handlers.put(name, handler = MethodHandler.EMPTY);
            }
        }
        return handler;
    }

    private static boolean handleProxyObjectMethod(Object proxy, Method m, MethodResult result) {
        switch (m.getName()) {
            case "castToType":
                if (m.getParameterTypes().length == 0) {
                    result.set(proxy);
                    return true;
                }
                break;
            case "newProxyBuilder":
                if (m.getReturnType() == ProxyBuilder.class
                        && m.getParameterTypes().length == 0) {
                    ProxyObject<?> proxyObject = (ProxyObject<?>) proxy;
                    ProxyBuilder<?> proxyBuilder = new ProxyBuilder<>(proxyObject);
                    result.set(proxyBuilder);
                    return true;
                }
                break;
        }
        return false;
    }

    // TODO: better implementation
    private static final class MethodHandlers implements MethodHandler {
        private final List<? extends NamedMethodHandler> handlers;
        private final ConcurrentHashMap<Method, NamedMethodHandler> cache;

        private MethodHandlers(List<? extends NamedMethodHandler> handlers) {
            this.handlers = handlers;
            this.cache = new ConcurrentHashMap<>(handlers.size(), 0.75f, 1);
        }

        private static MethodHandler create(List<? extends NamedMethodHandler> methodHandlers) {
            if (methodHandlers == null || methodHandlers.size() == 0) {
                return MethodHandler.EMPTY;
            } else if (methodHandlers.size() == 1) {
                return methodHandlers.get(0);
            }
            return new MethodHandlers(methodHandlers);
        }

        @Override
        public boolean handle(Object proxy, Method method, Object[] args, MethodResult result) throws Throwable {
            NamedMethodHandler cached = cache.get(method);
            if (cached != null) {
                return cached.handle(proxy, method, args, result);
            }
            for (NamedMethodHandler handler : handlers) {
                if (handler.handle(proxy, method, args, result)) {
                    synchronized (cache) {
                        cache.put(method, handler);
                    }
                    return true;
                }
            }
            return false;
        }
    }
}
