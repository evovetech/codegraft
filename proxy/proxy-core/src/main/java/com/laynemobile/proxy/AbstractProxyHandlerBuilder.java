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

import java.util.Locale;

public abstract class AbstractProxyHandlerBuilder<T> implements Builder<T> {
    public static final NamedMethodHandler handler(ProxyFunction<?> proxyFunction) {
        if (proxyFunction == null) {
            throw new IllegalStateException("proxy function must not be null");
        }
        NamedMethodHandler handler = proxyFunction.handler();
        if (handler == null) {
            String msg = String.format(Locale.US, "proxy function '%s' must return a non-null handler", proxyFunction);
            throw new IllegalStateException(msg);
        }
        return handler;
    }

    public abstract ProxyHandler<T> proxyHandler();

    public ProxyBuilder<T> builder() {
        return new ProxyBuilder<>(proxyHandler());
    }

    @Override public T build() {
        return builder()
                .build();
    }
}
