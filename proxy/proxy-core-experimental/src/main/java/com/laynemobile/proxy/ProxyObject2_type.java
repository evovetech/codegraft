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

import com.laynemobile.proxy.functions.ProxyFunc0Def;
import com.laynemobile.proxy.functions.transforms.ProxyFunc0Transform;

final class ProxyObject2_type<T> extends ProxyFunc0Def<ProxyObject2<T>, TypeToken<T>> {
    ProxyObject2_type() {
        super("type", new TypeToken<TypeToken<T>>() {});
    }

    @Override public Function<T> asFunction(ProxyFunc0Transform<ProxyObject2<T>, TypeToken<T>> transform) {
        return new Function<>(this, transform);
    }

    static class Function<T> extends ProxyFunc0Def.Function<ProxyObject2<T>, TypeToken<T>> {
        private Function(ProxyObject2_type<T> functionDef,
                ProxyFunc0Transform<ProxyObject2<T>, TypeToken<T>> function) {
            super(functionDef, function);
        }
    }
}
