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

import com.google.common.base.MoreObjects;
import com.laynemobile.proxy.functions.ProxyFunction2;
import com.laynemobile.proxy.functions.ProxyFunctionDef;
import com.laynemobile.proxy.internal.ProxyLog;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import static java.util.Collections.unmodifiableSortedSet;

public class ProxyBuilder2<T> implements Builder<ProxyObject2<T>> {
    private final TypeToken<T> type;
    private final SortedSet<ProxyType2<? extends T>> handlers;

    public ProxyBuilder2(TypeToken<T> type) {
        this.type = type;
        this.handlers = new TreeSet<>();
    }

    public ProxyBuilder2(ProxyType2<T> parent) {
        this(parent.type());
        handlers.add(parent);
    }

    public ProxyBuilder2(ProxyObject2<T> source) {
        this(source.type());
        handlers.addAll(source.proxyTypes());
    }

    public final ProxyBuilder2<T> add(ProxyType2<? extends T> handler) {
        throwIfContains(handler);
        this.handlers.add(handler);
        return this;
    }

    @SafeVarargs public final ProxyBuilder2<T> addAll(ProxyType2<? extends T>... handlers) {
        for (ProxyType2<? extends T> handler : handlers) {
            add(handler);
        }
        return this;
    }

    public final ProxyBuilder2<T> addAll(Collection<? extends ProxyType2<? extends T>> handlers) {
        for (ProxyType2<? extends T> handler : handlers) {
            add(handler);
        }
        return this;
    }

    public final boolean contains(Class<?> type) {
        for (ProxyType2<?> module : handlers) {
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

    @Override public final ProxyObject2<T> build() {
        if (handlers.isEmpty()) {
            throw new IllegalStateException("no handlers");
        } else if (!contains(type.getRawType())) {
            String msg = String.format(Locale.US, "must contain '%s' handler", type);
            throw new IllegalStateException(msg);
        }
        return build(type, handlers);
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

    private void throwIfContains(ProxyType2<?> handler) {
        throwIfContains(handler.type());
        for (ProxyType2<?> superType : handler.superTypes()) {
            throwIfContains(superType);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> ProxyObject2<T> build(TypeToken<T> baseType, SortedSet<ProxyType2<? extends T>> proxyTypes) {
        List<Class<?>> classes = new ArrayList<>(proxyTypes.size());
        Map<String, List<ProxyFunction2<?, ?, ?>>> handlers = new HashMap<>();

        // Add ProxyObject interface
        ProxyType2<ProxyObject2<T>> proxyObjectType = buildProxyObjectType(baseType, proxyTypes);
        addProxyType(proxyObjectType, classes, handlers);

        // add implemented interfaces
        for (ProxyType2<? extends T> proxyType : proxyTypes) {
            addProxyType(proxyType, classes, handlers);
        }

        // Verify we have every method implemented
        verifyFunctionsImplemented(proxyTypes, handlers);

        ClassLoader cl = baseType.getRawType().getClassLoader();
        Class[] ca = classes.toArray(new Class[classes.size()]);
        return (ProxyObject2<T>) Proxy.newProxyInstance(cl, ca, new ProxyInvocationHandler<>(baseType, handlers));
    }

    private static <T> void addProxyType(ProxyType2<? extends T> proxyType, List<Class<?>> classes,
            Map<String, List<ProxyFunction2<?, ?, ?>>> handlers) {
        ProxyLog.d("ProxyBuilder", "proxyType: %s", proxyType.type());
        classes.addAll(proxyType.rawTypes());
        for (ProxyFunction2<?, ?, ?> function : proxyType.allFunctions()) {
            final String name = function.name();
            ProxyLog.d("ProxyBuilder", "proxyType: %s, function: %s", proxyType.type(), name);
            List<ProxyFunction2<?, ?, ?>> current = handlers.get(name);
            if (current == null) {
                current = new ArrayList<>();
                handlers.put(name, current);
            }
            current.add(0, function);
        }
    }

    private static <T> ProxyType2<ProxyObject2<T>> buildProxyObjectType(TypeToken<T> baseType,
            SortedSet<ProxyType2<? extends T>> proxyTypes) {
        String toString = MoreObjects.toStringHelper("ProxyObject")
                .add("type", baseType)
                .add("proxyTypes", proxyTypes)
                .toString();
        return new ProxyObject2ProxyTypeBuilder<T>()
                .setType(baseType)
                .setProxyTypes(unmodifiableSortedSet(proxyTypes))
                .setCastToType()
                .setNewProxyBuilder()
                .setToString(toString)
                .buildProxyType();
    }

    private static <T> void verifyFunctionsImplemented(Collection<ProxyType2<? extends T>> proxyTypes,
            Map<String, List<ProxyFunction2<?, ?, ?>>> handlers) {
        for (ProxyType2<? extends T> proxyType : proxyTypes) {
            TypeDef2<? extends T> typeDef = proxyType.definition();
            for (ProxyFunctionDef<?, ?, ?> functionDef : typeDef.allFunctions()) {
                String name = functionDef.name();
                List<? extends ProxyFunction2<?, ?, ?>> implList = handlers.get(name);
                if (implList == null) {
                    throw new IllegalStateException("must implement " + functionDef);
                }
                boolean found = false;
                for (ProxyFunction2<?, ?, ?> functionImpl : implList) {
                    if (functionDef.equals(functionImpl.functionDef())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    throw new IllegalStateException("must implement " + functionDef);
                }
            }
        }
    }
}
