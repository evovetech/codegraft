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

import com.laynemobile.proxy.functions.ProxyFunction;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public interface ProxyType<T> extends BaseTypeDef<T, ProxyType<? super T>, ProxyFunction<?, ?>> {
    TypeDef<T> definition();

    final class Builder<T> implements com.laynemobile.proxy.Builder<ProxyType<T>> {
        private final TypeDef<T> typeDef;
        private final Set<ProxyType<? super T>> superTypes = new HashSet<>();
        private final LinkedHashSet<ProxyFunction<?, ?>> functions = new LinkedHashSet<>();

        Builder(TypeDef<T> typeDef) {
            if (typeDef == null) {
                throw new NullPointerException("typeDef is null");
            }
            this.typeDef = typeDef;
        }

        Builder(ProxyType<T> proxyType) {
            this(proxyType.definition());
            superTypes.addAll(proxyType.superTypes());
            functions.addAll(proxyType.functions());
        }

        public Builder<T> addSuperType(ProxyType<? super T> superType) {
            if (superType == null) {
                throw new NullPointerException("superType is null");
            }
            superTypes.add(superType);
            return this;
        }

        public Builder<T> addFunction(ProxyFunction<?, ?> function) {
            if (function == null) {
                throw new NullPointerException("function is null");
            }
            functions.add(function);
            return this;
        }

        @Override public ProxyType<T> build() {
            return new ConcreteProxyType<>(typeDef, superTypes, functions);
        }
    }
}
