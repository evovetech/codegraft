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

import com.laynemobile.proxy.functions.Func0;
import com.laynemobile.proxy.functions.transforms.Func0Transform;

import java.util.SortedSet;

final class ProxyObjectProxyTypeBuilder<T> {
    private final ProxyObjectDef<T> def = new ProxyObjectDef<>();

    private ProxyObject_type.Function type;

    private ProxyObject_proxyTypes.Function proxyTypes;

    private ProxyObject_toString.Function toString;

    ProxyObjectProxyTypeBuilder<T> setType(ProxyObject_type.Function<T> type) {
        this.type = type;
        return this;
    }

    ProxyObjectProxyTypeBuilder<T> setType(Func0Transform<TypeToken<T>> type) {
        return setType(def.type.asFunction(type));
    }

    ProxyObjectProxyTypeBuilder<T> setType(Func0<? extends TypeToken<T>> type) {
        return setType(new Func0Transform<>(type));
    }

    ProxyObjectProxyTypeBuilder<T> setType(TypeToken<T> type) {
        return setType(new Func0Transform<>(type));
    }

    ProxyObjectProxyTypeBuilder<T> setProxyTypes(ProxyObject_proxyTypes.Function<T> proxyTypes) {
        this.proxyTypes = proxyTypes;
        return this;
    }

    ProxyObjectProxyTypeBuilder<T> setProxyTypes(Func0Transform<SortedSet<ProxyType<? extends T>>> proxyTypes) {
        return setProxyTypes(def.proxyTypes.asFunction(proxyTypes));
    }

    ProxyObjectProxyTypeBuilder<T> setProxyTypes(Func0<? extends SortedSet<ProxyType<? extends T>>> proxyTypes) {
        return setProxyTypes(new Func0Transform<>(proxyTypes));
    }

    ProxyObjectProxyTypeBuilder<T> setProxyTypes(SortedSet<ProxyType<? extends T>> proxyTypes) {
        return setProxyTypes(new Func0Transform<>(proxyTypes));
    }

    ProxyObjectProxyTypeBuilder<T> setToString(ProxyObject_toString.Function toString) {
        this.toString = toString;
        return this;
    }

    ProxyObjectProxyTypeBuilder<T> setToString(Func0Transform<String> toString) {
        return setToString(def.toString.asFunction(toString));
    }

    ProxyObjectProxyTypeBuilder<T> setToString(Func0<? extends String> toString) {
        return setToString(new Func0Transform<>(toString));
    }

    ProxyObjectProxyTypeBuilder<T> setToString(String toString) {
        return setToString(new Func0Transform<>(toString));
    }

    ProxyType<ProxyObject<T>> buildProxyType() {
        return def.newProxyBuilder()
                .addFunction(type)
                .addFunction(proxyTypes)
                .addFunction(toString)
                .build();
    }
}
