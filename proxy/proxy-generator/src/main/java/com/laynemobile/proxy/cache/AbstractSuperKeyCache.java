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

import com.laynemobile.proxy.internal.ProxyLog;

import java.util.Map;

public abstract class AbstractSuperKeyCache<SK, K extends SK, V, P>
        extends AbstractParameterizedCache<K, V, P>
        implements SuperKeyCache<SK, K, V, P> {

    protected AbstractSuperKeyCache() {}

    protected AbstractSuperKeyCache(Map<K, V> cache) {
        super(cache);
    }

    protected abstract K cast(SK sk) throws Exception;

    @Override public final V parse(SK sk, P p) {
        try {
            K k = cast(sk);
            if (k != null) {
                return getOrCreate(k, p);
            }
        } catch (Exception e) {
            log(p, "error %s", ProxyLog.getStackTraceString(e));
        }
        return null;
    }
}
