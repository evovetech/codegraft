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

import com.laynemobile.proxy.AbstractProxyDef;
import com.laynemobile.proxy.TypeDef;

public class CacheDef<K, V> extends AbstractProxyDef<Cache<K, V>> {
    private final TypeDef<Cache<K, V>> typeDef = new TypeDef.Builder<Cache<K, V>>() {}
            .addFunction(new Cache_get__K.Def<K, V>())
            .addFunction(new Cache_values.Def<K, V>())
            .build();

    @Override public TypeDef<Cache<K, V>> typeDef() {
        return typeDef;
    }
//
//    private void play() {
//        typeDef.newProxyBuilder()
//                .addFunction()
//    }
}
