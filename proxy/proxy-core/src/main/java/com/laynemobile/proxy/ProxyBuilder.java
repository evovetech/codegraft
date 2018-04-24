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
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProxyBuilder<T> implements Builder<T> {
    private final TypeToken<T> type;
    private final List<ProxyHandler<? extends T>> handlers;

    public ProxyBuilder(TypeToken<T> type) {
        this.type = type;
        this.handlers = new ArrayList<>();
    }

    public ProxyBuilder(ProxyHandler<T> parent) {
        this(parent.type());
        handlers.add(parent);
    }

    public final ProxyBuilder<T> add(ProxyHandler<? extends T> handler) {
        throwIfContains(handler);
        this.handlers.add(handler);
        return this;
    }

    @SafeVarargs public final ProxyBuilder<T> addAll(ProxyHandler<? extends T>... handlers) {
        for (ProxyHandler<? extends T> handler : handlers) {
            add(handler);
        }
        return this;
    }

    public final ProxyBuilder<T> addAll(List<? extends ProxyHandler<? extends T>> handlers) {
        for (ProxyHandler<? extends T> handler : handlers) {
            add(handler);
        }
        return this;
    }

    public final boolean contains(Class<?> type) {
        for (ProxyHandler<?> module : handlers) {
            if (module.rawTypes().contains(type)) {
                return true;
            }
        }
        return false;
    }

    public final void verifyContains(Class<?> type) {
        if (!contains(type)) {
            String msg = String.format("builder must have type '%s'. You might have forgot a module", type);
            throw new IllegalStateException(msg);
        }
    }

    public final void verifyContains(Collection<? extends Class<?>> types) {
        for (Class<?> type : types) {
            verifyContains(type);
        }
    }

    @Override public final T build() {
        if (handlers.isEmpty()) {
            throw new IllegalStateException("no handlers");
        } else if (!contains(type.getRawType())) {
            String msg = String.format(Locale.US, "must contain '%s' handler", type);
            throw new IllegalStateException(msg);
        }
        return create(type, handlers);
    }

    private boolean contains(TypeToken<?> type) {
        return contains(type.getRawType());
    }

    private void throwIfContains(TypeToken<?> type) {
        if (contains(type)) {
            String msg = String.format("handler type '%s' already defined", type);
            throw new IllegalStateException(msg);
        }
    }

    private void throwIfContains(ProxyHandler<?> handler) {
        throwIfContains(handler.type());
        for (TypeToken<?> superType : handler.superTypes()) {
            throwIfContains(superType);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T create(TypeToken<T> baseType, Collection<ProxyHandler<? extends T>> extensions) {
        List<Class<?>> classes = new ArrayList<>(extensions.size());
        Map<String, List<MethodHandler>> handlers = new HashMap<>();
        for (ProxyHandler<? extends T> extension : extensions) {
            classes.addAll(extension.rawTypes());
            for (Map.Entry<String, List<MethodHandler>> entry : extension.handlers().entrySet()) {
                final String name = entry.getKey();
                List<MethodHandler> current = handlers.get(name);
                if (current == null) {
                    current = new ArrayList<>();
                    handlers.put(name, current);
                }
                current.addAll(entry.getValue());
            }
        }
        ClassLoader cl = baseType.getRawType().getClassLoader();
        Class[] ca = classes.toArray(new Class[classes.size()]);
        return (T) Proxy.newProxyInstance(cl, ca, new InvokeHandler(handlers));
    }

    private static class InvokeHandler implements InvocationHandler {
        private static final String TAG = InvokeHandler.class.getSimpleName();

        private final ConcurrentHashMap<String, MethodHandler> handlers;

        private InvokeHandler(Map<String, List<MethodHandler>> _handlers) {
            ConcurrentHashMap<String, MethodHandler> handlers = new ConcurrentHashMap<>(_handlers.size(), 0.75f, 1);
            for (Map.Entry<String, List<MethodHandler>> entry : _handlers.entrySet()) {
                handlers.put(entry.getKey(), MethodHandlers.create(entry.getValue()));
            }
            this.handlers = handlers;
        }

        @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            ProxyLog.d(TAG, "calling method: %s", method);
            MethodResult result = new MethodResult();
            if (get(method).handle(proxy, method, args, result)) {
                Object r = result.get();
                ProxyLog.d(TAG, "handled method: %s, result: %s", method, r);
                return r;
            }
            ProxyLog.w(TAG, "could not find handler for method: %s", method);
            return null;
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
    }

    private static class MethodHandlers implements MethodHandler {
        private final List<MethodHandler> handlers;

        private MethodHandlers(List<MethodHandler> handlers) {
            this.handlers = handlers;
        }

        private static MethodHandler create(List<MethodHandler> methodHandlers) {
            if (methodHandlers == null || methodHandlers.size() == 0) {
                return MethodHandler.EMPTY;
            } else if (methodHandlers.size() == 1) {
                return methodHandlers.get(0);
            }
            return new MethodHandlers(methodHandlers);
        }

        @Override
        public boolean handle(Object proxy, Method method, Object[] args, MethodResult result) throws Throwable {
            for (MethodHandler handler : handlers) {
                if (handler.handle(proxy, method, args, result)) {
                    return true;
                }
            }
            return false;
        }
    }
}
