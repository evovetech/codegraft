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

import sourcerer.processor.Env;

public abstract class EnvCache<K, V> extends AbstractParameterizedCache<K, V, Env> {
    protected EnvCache() {}

    protected EnvCache(Map<K, V> cache) {
        super(cache);
    }

    public static <K, V> EnvCache<K, V> create(final Creator<K, V> creator) {
        return new EnvCache<K, V>() {
            @Override protected V create(K k, Env env) {
                return creator.create(k, env);
            }
        };
    }

    @Override protected void log(Env env, String format, Object... args) {
//        env.log(format, args);
    }

    public interface Creator<K, V> extends ParameterizedCache.Creator<K, V, Env> {}
}
