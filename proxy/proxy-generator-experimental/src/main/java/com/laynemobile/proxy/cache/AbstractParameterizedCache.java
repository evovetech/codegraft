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

package com.laynemobile.proxy.cache;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractParameterizedCache<K, V, P>
        extends AbstractCache<K, V>
        implements ParameterizedCache<K, V, P> {

    private final ThreadLocal<Map<K, P>> localParams = new ThreadLocal<Map<K, P>>() {
        @Override protected Map<K, P> initialValue() {
            return new HashMap<>();
        }
    };

    protected AbstractParameterizedCache() {}

    protected AbstractParameterizedCache(Map<K, V> cache) {
        super(cache);
    }

    public static <K, V, P> AbstractParameterizedCache<K, V, P> create(
            final ParameterizedCache.Creator<K, V, P> creator) {
        return new AbstractParameterizedCache<K, V, P>() {
            @Override protected V create(K k, P p) {
                return creator.create(k, p);
            }
        };
    }

    protected abstract V create(K k, P p);

    protected void log(P p, String format, Object... args) {
//        super.log(format, args);
    }

    @Override public final V get(K key) {
        return getIfPresent(key);
    }

    @Override protected final V create(K key) {
        final P param = getParam(key);
        if (param == null) {
            throw new IllegalArgumentException("p cannot be null");
        }
        return create(key, param);
    }

    @Override public final V getOrCreate(K k, P p) {
        if (p == null) {
            throw new IllegalArgumentException("p cannot be null");
        }

        V v;
        if ((v = getIfPresent(k)) != null) {
            log(p, "returning cached value: %s", v);
            return v;
        }

        boolean put;
        Map<K, P> params = localParams.get();
        if (put = !params.containsKey(k)) {
            params.put(k, p);
        }

        try {
            return super.get(k);
        } finally {
            if (put) {
                params.remove(k);
            }
        }
    }

    @Override protected final void log(String format, Object... args) {
        final P p = getParam();
        if (p == null) {
            super.log(format, args);
        } else {
            log(p, format, args);
        }
    }

    private P getParam(K key) {
        return localParams.get().get(key);
    }

    private P getParam() {
        for (P param : localParams.get().values()) {
            if (param != null) {
                return param;
            }
        }
        return null;
    }
}
