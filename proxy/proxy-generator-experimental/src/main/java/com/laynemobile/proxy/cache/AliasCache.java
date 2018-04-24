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

import com.laynemobile.proxy.Alias;

import java.util.Map;

import sourcerer.processor.Env;

import static com.laynemobile.proxy.Util.runtime;

public abstract class AliasCache<K extends SK, V extends Alias<?>, SK>
        extends EnvCache<K, V> {
    protected AliasCache() {}

    protected AliasCache(Map<K, V> cache) {
        super(cache);
    }

    protected abstract K cast(SK sk, Env env) throws Exception;

    public final V parse(SK superType, Env env) {
        try {
            K k = cast(superType, env);
            if (k != null) {
                return getOrCreate(k, env);
            }
        } catch (Exception e) {
            throw runtime(e, "error parsing for key: %s, keytype: %s", superType, superType.getClass());
        }
        return null;
    }
}
