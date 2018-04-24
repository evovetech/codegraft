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

package com.laynemobile.api.cache;

import com.laynemobile.api.cache.CacheSpec.State;
import com.laynemobile.proxy.TypeToken;
import com.laynemobile.proxy.annotations.Generated;
import com.laynemobile.proxy.functions.AbstractProxyFunction;
import com.laynemobile.proxy.functions.ConcreteFunctionDef;
import com.laynemobile.proxy.functions.Func0;
import com.laynemobile.proxy.functions.Func1;

import java.util.Collection;

@Generated
public class Cache_values<K, V> extends AbstractProxyFunction<Cache_values.Transform<K, V>, Collection<V>> {

    public Cache_values(Transform<K, V> function) {
        super(new Def<K, V>(), function);
    }

    public static class Def<K, V> extends ConcreteFunctionDef<Collection<V>> {
        public Def() {
            super("get", new TypeToken<Collection<V>>() {}, new TypeToken<?>[]{
                    new TypeToken<K>() {}
            });
        }
    }

    public static class Transform<K, V> extends ProxyFunc0Transform<State<K, V>, Collection<V>> {
        public Transform(Func1<? super State<K, V>, ? extends Collection<V>> function) {
            super(function);
        }

        public Transform(Func0<? extends Collection<V>> function) {
            super(function);
        }

        public Transform(Collection<V> value) {
            super(value);
        }
    }
}
