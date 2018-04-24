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

import com.laynemobile.proxy.AbstractProxyTypeBuilder;
import com.laynemobile.proxy.ProxyType;
import com.laynemobile.proxy.annotations.Generated;
import com.laynemobile.proxy.functions.Func1;
import com.laynemobile.proxy.functions.Func2;

import java.util.Collection;

@Generated
public class CacheProxyTypeBuilder<K, V> extends AbstractProxyTypeBuilder<Cache<K, V>> {
    private Cache_get__K<? extends K, ? extends V> get;

    private Cache_values<? extends K, ? extends V> values;

    public CacheProxyTypeBuilder<K, V> setGet(Cache_get__K<? extends K, ? extends V> get) {
        this.get = get;
        return this;
    }

    public CacheProxyTypeBuilder<K, V> setGet(Cache_get__K.Transform<K, V> get) {
        return setGet(new Cache_get__K<>(get));
    }

    public CacheProxyTypeBuilder<K, V> setGet(Func2<? super CacheSpec.State<K, V>, ? super K, ? extends V> get) {
        return setGet(new Cache_get__K.Transform<>(get));
    }

    public CacheProxyTypeBuilder<K, V> setValues(Cache_values<? extends K, ? extends V> values) {
        this.values = values;
        return this;
    }

    public CacheProxyTypeBuilder<K, V> setValues(Cache_values.Transform<K, V> values) {
        return setValues(new Cache_values<>(values));
    }

    public CacheProxyTypeBuilder<K, V> setValues(Func1<? super CacheSpec.State<K, V>, ? extends Collection<V>> values) {
        return setValues(new Cache_values.Transform<>(values));
    }

    // TODO: inject CacheSpec.State into first param
    @Override public ProxyType<Cache<K, V>> buildProxyType() {
        return new CacheDef<K, V>().newProxyBuilder()
                .addFunction(get)
                .addFunction(values)
                .build();
    }

//  @Override
//  public ProxyHandler<Cache<K, V>> proxyHandler() {
//    return ProxyHandler.builder(new TypeToken<Cache<K, V>>() {})
//        .handle(handler(get))
//        .handle(handler(values))
//        .build();
//  }
}
