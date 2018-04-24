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

public abstract class MultiAliasCache<K1, K2, V extends Alias<?>>
        extends AbstractMultiCache<K1, EnvCache<K2, V>, Env, K2, V> {
    protected MultiAliasCache() {}

    protected MultiAliasCache(Map<K1, EnvCache<K2, V>> cache) {
        super(cache);
    }

    @Override protected void log(Env env, String format, Object... args) {
//        env.log(format, args);
    }

    public static <K1, K2, V extends Alias<?>> MultiAliasCache<K1, K2, V> create(
            final ValueCreator<K1, K2, V> creator) {
        return new MultiAliasCache<K1, K2, V>() {
            @Override protected EnvCache<K2, V> create(K1 k1, Env env) {
                return new ChildCache<>(k1, creator);
            }
        };
    }

    private static final class ChildCache<K1, K2, V extends Alias<?>> extends EnvCache<K2, V> {
        private final K1 k1;
        private final ValueCreator<K1, K2, V> creator;

        private ChildCache(K1 k1, ValueCreator<K1, K2, V> creator) {
            this.k1 = k1;
            this.creator = creator;
        }

        @Override protected V create(K2 k2, Env env) {
            return creator.create(k1, k2, env);
        }
    }

    public interface ValueCreator<K1, K2, V extends Alias<?>>
            extends MultiCache.ValueCreator<K1, K2, V, Env> {}
}
