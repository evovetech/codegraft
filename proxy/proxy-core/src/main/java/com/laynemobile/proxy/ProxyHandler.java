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

import com.google.common.base.Objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ProxyHandler<T> {
    private final TypeToken<T> type;
    private final Set<TypeToken<? super T>> superTypes;
    private final Map<String, List<MethodHandler>> handlers;

    private ProxyHandler(Builder<T> builder) {
        this.type = builder.type;
        this.superTypes = Collections.unmodifiableSet(builder.superTypes);
        this.handlers = Collections.unmodifiableMap(builder.handlers);
    }

    public static <T> Builder<T> builder(Class<T> type) {
        return builder(TypeToken.get(type));
    }

    public static <T> Builder<T> builder(TypeToken<T> type) {
        return new Builder<>(type);
    }

    public TypeToken<T> type() {
        return type;
    }

    public Set<TypeToken<? super T>> superTypes() {
        return superTypes;
    }

    public List<Class<?>> rawTypes() {
        List<Class<?>> rawTypes = new ArrayList<>(superTypes.size() + 1);
        rawTypes.add(type.getRawType());
        for (TypeToken<? super T> superType : superTypes) {
            rawTypes.add(superType.getRawType());
        }
        return rawTypes;
    }

    public Map<String, List<MethodHandler>> handlers() {
        return handlers;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProxyHandler)) return false;
        ProxyHandler<?> that = (ProxyHandler<?>) o;
        return Objects.equal(type, that.type) &&
                Objects.equal(superTypes, that.superTypes);
    }

    @Override public int hashCode() {
        return Objects.hashCode(type, superTypes);
    }

    public static final class Builder<T> {
        private final TypeToken<T> type;
        private final Set<TypeToken<? super T>> superTypes = new HashSet<>();
        private final Map<String, List<MethodHandler>> handlers = new HashMap<>();

        private Builder(TypeToken<T> type) {
            this.type = type;
        }

        public Builder<T> addParent(ProxyHandler<? super T> parent) {
            superTypes.add(parent.type);
            superTypes.addAll(parent.superTypes);
            handlers.putAll(parent.handlers);
            return this;
        }

        public MethodBuilder<T> method(String methodName) {
            return new MethodBuilder<>(this, methodName);
        }

        public Builder<T> handle(NamedMethodHandler handler) {
            return method(handler.name())
                    .handle(handler)
                    .add();
        }

        public Builder<T> handle(String methodName, MethodHandler handler) {
            return method(methodName)
                    .handle(handler)
                    .add();
        }

        public ProxyHandler<T> build() {
            return new ProxyHandler<>(this);
        }

        private Builder<T> add(MethodBuilder<T> builder) {
            List<MethodHandler> handlerList = this.handlers.get(builder.methodName);
            if (handlerList == null) {
                handlerList = new ArrayList<>();
                this.handlers.put(builder.methodName, handlerList);
            }
            handlerList.add(builder.handler);
            return this;
        }
    }

    public static final class MethodBuilder<T> {
        private final Builder<T> builder;
        private final String methodName;
        private MethodHandler handler;

        private MethodBuilder(Builder<T> builder, String methodName) {
            this.builder = builder;
            this.methodName = methodName;
        }

        public MethodBuilder<T> handle(MethodHandler handler) {
            this.handler = handler;
            return this;
        }

        public Builder<T> add() {
            if (handler == null) {
                throw new IllegalArgumentException("must set a handler");
            }
            return builder.add(this);
        }
    }
}
