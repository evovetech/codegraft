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

public abstract class AbstractProxyType2Builder<T> implements Builder<ProxyObject2<T>> {
    public abstract ProxyType2<T> buildProxyType();

    public ProxyBuilder2<T> proxyBuilder() {
        return new ProxyBuilder2<>(buildProxyType());
    }

    public ProxyCompleter2<T> proxyCompleter() {
        return new ProxyCompleter2<>(buildProxyType());
    }

    @Override public ProxyObject2<T> build() {
        return proxyBuilder()
                .build();
    }
}
