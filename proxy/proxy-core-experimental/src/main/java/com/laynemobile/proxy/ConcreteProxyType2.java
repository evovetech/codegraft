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

import com.laynemobile.proxy.functions.ProxyFunction2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.unmodifiableList;

final class ConcreteProxyType2<T> extends AbstractTypeDef2<T, ProxyType2<? super T>, ProxyFunction2<? super T, ?, ?>>
        implements ProxyType2<T> {
    private final TypeDef2<T> definition;
    private final List<? extends ProxyFunction2<? super T, ?, ?>> functions;

    ConcreteProxyType2(TypeDef2<T> definition, Collection<? extends ProxyType2<? super T>> superTypes,
            Collection<? extends ProxyFunction2<? super T, ?, ?>> functions) {
        super(definition.type(), superTypes);
        this.definition = definition;
        this.functions = unmodifiableList(new ArrayList<>(functions));
    }

    @Override public TypeDef2<T> definition() {
        return definition;
    }

    @Override public final List<? extends ProxyFunction2<? super T, ?, ?>> functions() {
        return functions;
    }

    @Override public Builder<T> newProxyBuilder() {
        return new Builder<>(this);
    }
}
