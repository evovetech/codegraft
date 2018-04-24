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

package com.laynemobile.proxy;

final class ProxyObject2Def<T> extends AbstractProxyDef2<ProxyObject2<T>> {
    final ProxyObject2_type<T> type = new ProxyObject2_type<>();
    final ProxyObject2_proxyTypes<T> proxyTypes = new ProxyObject2_proxyTypes<>();
    final ProxyObject2_castToType<T> castToType = new ProxyObject2_castToType<>();
    final ProxyObject2_newProxyBuilder<T> newProxyBuilder = new ProxyObject2_newProxyBuilder<>();
    final ProxyObject2_toString<T> toString = new ProxyObject2_toString<>();
    final TypeDef2<ProxyObject2<T>> typeDef = new TypeDef2.Builder<ProxyObject2<T>>() {}
            .addFunction(type)
            .addFunction(proxyTypes)
            .addFunction(castToType)
            .addFunction(newProxyBuilder)
            .addFunction(toString)
            .build();

    @Override public TypeDef2<ProxyObject2<T>> typeDef() {
        return typeDef;
    }
}
