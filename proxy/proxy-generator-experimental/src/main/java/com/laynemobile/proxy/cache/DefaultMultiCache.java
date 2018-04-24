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

public final class DefaultMultiCache<K1, K2, V, P> extends AbstractMultiCache<K1, ParameterizedCache<K2, V, P>, P, K2, V> {
    private final MultiCache.ValueCreator<K1, K2, V, P> creator;

    private DefaultMultiCache(ValueCreator<K1, K2, V, P> creator) {
        this.creator = creator;
    }

    public static <K1, K2, V, P> DefaultMultiCache<K1, K2, V, P> create(ValueCreator<K1, K2, V, P> creator) {
        return new DefaultMultiCache<>(creator);
    }

    @Override protected ParameterizedCache<K2, V, P> create(K1 k1, P p) {
        return new ChildCache(k1);
    }

    private final class ChildCache extends AbstractParameterizedCache<K2, V, P> {
        private final K1 k1;

        private ChildCache(K1 k1) {
            this.k1 = k1;
        }

        @Override protected V create(K2 k2, P p) {
            return creator.create(k1, k2, p);
        }
    }
}
