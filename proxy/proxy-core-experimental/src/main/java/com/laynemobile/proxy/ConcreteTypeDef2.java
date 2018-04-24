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

import com.laynemobile.proxy.functions.ProxyFunctionDef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.unmodifiableList;

final class ConcreteTypeDef2<T> extends AbstractTypeDef2<T, TypeDef2<? super T>, ProxyFunctionDef<? super T, ?, ?>>
        implements TypeDef2<T> {
    private final List<? extends ProxyFunctionDef<? super T, ?, ?>> functions;

    ConcreteTypeDef2(TypeToken<T> type, Collection<? extends TypeDef2<? super T>> superTypes,
            Collection<? extends ProxyFunctionDef<? super T, ?, ?>> functions) {
        super(type, superTypes);
        this.functions = unmodifiableList(new ArrayList<>(functions));
    }

    ConcreteTypeDef2(TypeDef2<T> typeDef) {
        this(typeDef.type(), typeDef.superTypes(), typeDef.functions());
    }

    @Override public final List<? extends ProxyFunctionDef<? super T, ?, ?>> functions() {
        return functions;
    }

    @Override public final ProxyType2.Builder<T> newProxyBuilder() {
        return new ProxyType2.Builder<>(this);
    }
}
