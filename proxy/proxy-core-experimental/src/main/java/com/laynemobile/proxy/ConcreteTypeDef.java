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

import com.laynemobile.proxy.functions.FunctionDef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.unmodifiableList;

final class ConcreteTypeDef<T> extends AbstractTypeDef<T, TypeDef<? super T>, FunctionDef<?, ?>>
        implements TypeDef<T> {
    private final List<? extends FunctionDef<?, ?>> functions;

    ConcreteTypeDef(TypeToken<T> type, Collection<? extends TypeDef<? super T>> superTypes,
            Collection<? extends FunctionDef<?, ?>> functions) {
        super(type, superTypes);
        this.functions = unmodifiableList(new ArrayList<>(functions));
    }

    ConcreteTypeDef(TypeDef<T> typeDef) {
        this(typeDef.type(), typeDef.superTypes(), typeDef.functions());
    }

    @Override public final List<? extends FunctionDef<?, ?>> functions() {
        return functions;
    }

    @Override public final ProxyType.Builder<T> newProxyBuilder() {
        return new ProxyType.Builder<>(this);
    }
}
