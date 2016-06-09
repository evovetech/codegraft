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

package sourcerer.types;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import sourcerer.Builder;
import sourcerer.internal.Util;

public final class TypeBuilder<T> implements Builder<T> {
    private final Class<T> baseType;
    private final Set<TypeHandler<? extends T>> modules = new HashSet<>();

    public TypeBuilder(Class<T> baseType) {
        this.baseType = baseType;
    }

    public final TypeBuilder<T> module(TypeHandler<? extends T> module) {
        if (this.modules.contains(module)) {
            String msg = String.format("source type '%s' already defined", module.type);
            throw new IllegalStateException(msg);
        }
        this.modules.add(module);
        return this;
    }

    @SafeVarargs public final TypeBuilder<T> modules(TypeHandler<? extends T>... modules) {
        return modules(Arrays.asList(modules));
    }

    public final TypeBuilder<T> modules(List<? extends TypeHandler<? extends T>> modules) {
        for (TypeHandler<? extends T> module : modules) {
            module(module);
        }
        return this;
    }

    public final boolean contains(Class<? extends T> type) {
        for (TypeHandler<? extends T> module : modules) {
            if (module.type.equals(type)) {
                return true;
            }
        }
        return false;
    }

    public final void verifyContains(Class<? extends T> type) {
        if (!contains(type)) {
            String msg = String.format("builder must have type '%s'. You might have forgot a module", type);
            throw new IllegalStateException(msg);
        }
    }

    public final void verifyContains(Collection<Class<? extends T>> types) {
        for (Class<? extends T> type : types) {
            verifyContains(type);
        }
    }

    @Override public final T build() {
        if (modules.isEmpty()) {
            throw new IllegalStateException("no modules");
        } else if (!contains(baseType)) {
            String msg = String.format(Locale.US, "must contain '%s' module", baseType);
            throw new IllegalStateException(msg);
        }
        return create(baseType, modules);
    }

    @SuppressWarnings("unchecked")
    private static <T> T create(Class<T> baseType, Collection<TypeHandler<? extends T>> extensions) {
        Set<Class<? extends T>> classes = new HashSet<>();
        Map<String, List<sourcerer.types.MethodHandler>> handlers = new HashMap<>();
        for (TypeHandler<? extends T> extension : extensions) {
            classes.add(extension.type);
            for (Map.Entry<String, List<MethodHandler>> entry : extension.handlers.entrySet()) {
                final String name = entry.getKey();
                List<sourcerer.types.MethodHandler> current = handlers.get(name);
                if (current == null) {
                    current = new ArrayList<>();
                    handlers.put(name, current);
                }
                current.addAll(entry.getValue());
            }
        }
        ClassLoader cl = baseType.getClassLoader();
        Class[] ca = classes.toArray(new Class[classes.size()]);
        return (T) Proxy.newProxyInstance(cl, ca, new InvokeHandler(handlers));
    }

    private static class InvokeHandler implements InvocationHandler {
        private static final String TAG = InvokeHandler.class.getSimpleName();

        private final Map<String, List<MethodHandler>> handlers;

        private InvokeHandler(Map<String, List<MethodHandler>> handlers) {
            this.handlers = Collections.unmodifiableMap(handlers);
        }

        @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//            Log.d(TAG, "calling method: %s", method);
            MethodResult result = new MethodResult();
            List<MethodHandler> handlers = this.handlers.get(method.getName());
            for (MethodHandler handler : Util.nullSafe(handlers)) {
                if (handler.handle(proxy, method, args, result)) {
                    Object r = result.get();
//                    Log.d(TAG, "handled method: %s, result: %s", method, r);
                    return r;
                }
            }
//            Log.w(TAG, "could not find handler for method: %s", method);
            return null;
        }
    }
}
