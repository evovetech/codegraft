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
import com.laynemobile.proxy.functions.Func1;
import com.laynemobile.proxy.functions.transforms.ProxyFunc0Transform;

import java.util.SortedSet;

final class ProxyObject2ProxyTypeBuilder<T> {
    private final ProxyObject2Def<T> def = new ProxyObject2Def<>();

    private ProxyObject2_type.Function<T> type;

    private ProxyObject2_proxyTypes.Function<T> proxyTypes;

    private ProxyObject2_castToType.Function<T> castToType;

    private ProxyObject2_newProxyBuilder.Function<T> newProxyBuilder;

    private ProxyObject2_toString.Function<T> toString;

    ProxyObject2ProxyTypeBuilder<T> setType(ProxyObject2_type.Function<T> type) {
        this.type = type;
        return this;
    }

    ProxyObject2ProxyTypeBuilder<T> setType(ProxyFunc0Transform<ProxyObject2<T>, TypeToken<T>> type) {
        return setType(def.type.asFunction(type));
    }

    ProxyObject2ProxyTypeBuilder<T> setType(Func1<? super ProxyObject2<T>, ? extends TypeToken<T>> type) {
        return setType(new ProxyFunc0Transform<>(type));
    }

    ProxyObject2ProxyTypeBuilder<T> setType(Func0<? extends TypeToken<T>> type) {
        return setType(new ProxyFunc0Transform<ProxyObject2<T>, TypeToken<T>>(type));
    }

    ProxyObject2ProxyTypeBuilder<T> setType(TypeToken<T> type) {
        return setType(new ProxyFunc0Transform<ProxyObject2<T>, TypeToken<T>>(type));
    }

    ProxyObject2ProxyTypeBuilder<T> setProxyTypes(ProxyObject2_proxyTypes.Function<T> proxyTypes) {
        this.proxyTypes = proxyTypes;
        return this;
    }

    ProxyObject2ProxyTypeBuilder<T> setProxyTypes(
            ProxyFunc0Transform<ProxyObject2<T>, SortedSet<ProxyType2<? extends T>>> proxyTypes) {
        return setProxyTypes(def.proxyTypes.asFunction(proxyTypes));
    }

    ProxyObject2ProxyTypeBuilder<T> setProxyTypes(
            Func1<? super ProxyObject2<T>, ? extends SortedSet<ProxyType2<? extends T>>> proxyTypes) {
        return setProxyTypes(new ProxyFunc0Transform<>(proxyTypes));
    }

    ProxyObject2ProxyTypeBuilder<T> setProxyTypes(Func0<? extends SortedSet<ProxyType2<? extends T>>> proxyTypes) {
        return setProxyTypes(new ProxyFunc0Transform<ProxyObject2<T>, SortedSet<ProxyType2<? extends T>>>(proxyTypes));
    }

    ProxyObject2ProxyTypeBuilder<T> setProxyTypes(SortedSet<ProxyType2<? extends T>> proxyTypes) {
        return setProxyTypes(new ProxyFunc0Transform<ProxyObject2<T>, SortedSet<ProxyType2<? extends T>>>(proxyTypes));
    }

    ProxyObject2ProxyTypeBuilder<T> setCastToType(ProxyObject2_castToType.Function<T> castToType) {
        this.castToType = castToType;
        return this;
    }

    ProxyObject2ProxyTypeBuilder<T> setCastToType(ProxyFunc0Transform<ProxyObject2<T>, T> castToType) {
        return setCastToType(def.castToType.asFunction(castToType));
    }

    ProxyObject2ProxyTypeBuilder<T> setCastToType(Func1<? super ProxyObject2<T>, ? extends T> castToType) {
        return setCastToType(new ProxyFunc0Transform<>(castToType));
    }

    ProxyObject2ProxyTypeBuilder<T> setCastToType() {
        return setCastToType(new Func1<ProxyObject2<T>, T>() {
            @SuppressWarnings("unchecked")
            @Override public T call(ProxyObject2<T> tProxyObject2) {
                return (T) tProxyObject2;
            }
        });
    }

    ProxyObject2ProxyTypeBuilder<T> setNewProxyBuilder(ProxyObject2_newProxyBuilder.Function<T> newProxyBuilder) {
        this.newProxyBuilder = newProxyBuilder;
        return this;
    }

    ProxyObject2ProxyTypeBuilder<T> setNewProxyBuilder(
            ProxyFunc0Transform<ProxyObject2<T>, ProxyBuilder2<T>> newProxyBuilder) {
        return setNewProxyBuilder(def.newProxyBuilder.asFunction(newProxyBuilder));
    }

    ProxyObject2ProxyTypeBuilder<T> setNewProxyBuilder(
            Func1<? super ProxyObject2<T>, ? extends ProxyBuilder2<T>> newProxyBuilder) {
        return setNewProxyBuilder(new ProxyFunc0Transform<>(newProxyBuilder));
    }

    ProxyObject2ProxyTypeBuilder<T> setNewProxyBuilder() {
        return setNewProxyBuilder(new Func1<ProxyObject2<T>, ProxyBuilder2<T>>() {
            @Override public ProxyBuilder2<T> call(ProxyObject2<T> tProxyObject2) {
                return new ProxyBuilder2<>(tProxyObject2);
            }
        });
    }

    ProxyObject2ProxyTypeBuilder<T> setToString(ProxyObject2_toString.Function<T> toString) {
        this.toString = toString;
        return this;
    }

    ProxyObject2ProxyTypeBuilder<T> setToString(ProxyFunc0Transform<ProxyObject2<T>, String> toString) {
        return setToString(def.toString.asFunction(toString));
    }

    ProxyObject2ProxyTypeBuilder<T> setToString(Func1<? super ProxyObject2<T>, ? extends String> toString) {
        return setToString(new ProxyFunc0Transform<>(toString));
    }

    ProxyObject2ProxyTypeBuilder<T> setToString(Func0<? extends String> toString) {
        return setToString(new ProxyFunc0Transform<ProxyObject2<T>, String>(toString));
    }

    ProxyObject2ProxyTypeBuilder<T> setToString(String toString) {
        return setToString(new ProxyFunc0Transform<ProxyObject2<T>, String>(toString));
    }

    ProxyType2<ProxyObject2<T>> buildProxyType() {
        return def.newProxyBuilder()
                .addFunction(type)
                .addFunction(proxyTypes)
                .addFunction(castToType)
                .addFunction(newProxyBuilder)
                .addFunction(toString)
                .build();
    }
}
