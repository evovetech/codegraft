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

final class ProxyObjectDef<T> extends AbstractProxyDef<ProxyObject<T>> {
    final ProxyObject_type<T> type = new ProxyObject_type<>();
    final ProxyObject_proxyTypes<T> proxyTypes = new ProxyObject_proxyTypes<>();
    final ProxyObject_toString toString = new ProxyObject_toString();
    final TypeDef<ProxyObject<T>> typeDef = new TypeDef.Builder<ProxyObject<T>>() {}
            .addFunction(type)
            .addFunction(proxyTypes)
            .addFunction(toString)
            .build();

    @Override public TypeDef<ProxyObject<T>> typeDef() {
        return typeDef;
    }
}
