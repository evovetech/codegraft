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

import com.laynemobile.proxy.model.Alias;

import java.util.Map;

import sourcerer.processor.Env;

public abstract class AliasSubtypeCache<K extends SK, V extends Alias<SV>, SK, SV extends Alias<?>>
        extends AliasCache<K, V, SK> {

    private final AliasCache<K, ? extends SV, SK> superCache;

    protected AliasSubtypeCache(AliasCache<K, ? extends SV, SK> superCache) {
        this.superCache = superCache;
    }

    protected AliasSubtypeCache(Map<K, V> cache, AliasCache<K, ? extends SV, SK> superCache) {
        super(cache);
        this.superCache = superCache;
    }

    protected abstract V create(SV sv, Env env);

    @Override protected K cast(SK sk, Env env) throws Exception {
        return superCache.cast(sk, env);
    }

    @Override protected final V create(K k, Env env) {
        return create(superCache.getOrCreate(k, env), env);
    }
}
