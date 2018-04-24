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

import java.util.Map;

public abstract class AbstractMultiCache<K1, V1 extends ParameterizedCache<K2, ? extends V2, P>, P, K2, V2>
        extends AbstractParameterizedCache<K1, V1, P>
        implements MultiCache<K1, V1, P, K2, V2> {
    protected AbstractMultiCache() {}

    protected AbstractMultiCache(Map<K1, V1> cache) {
        super(cache);
    }

    @Override public final V2 get(K1 k1, K2 k2) {
        V1 cache = get(k1);
        return cache == null ? null : cache.get(k2);
    }

    @Override public final V2 getOrCreate(K1 k1, K2 k2, P p) {
        return getOrCreate(k1, p)
                .getOrCreate(k2, p);
    }
}
