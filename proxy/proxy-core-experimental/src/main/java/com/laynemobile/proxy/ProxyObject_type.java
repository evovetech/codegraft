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

import com.laynemobile.proxy.functions.Func0Def;
import com.laynemobile.proxy.functions.transforms.Func0Transform;

final class ProxyObject_type<T> extends Func0Def<TypeToken<T>> {
    ProxyObject_type() {
        super("type", new TypeToken<TypeToken<T>>() {});
    }

    @Override public Function<T> asFunction(Func0Transform<TypeToken<T>> transform) {
        return new Function<>(this, transform);
    }

    static class Function<T> extends Func0Def.Function<TypeToken<T>> {
        private Function(ProxyObject_type<T> functionDef, Func0Transform<TypeToken<T>> function) {
            super(functionDef, function);
        }
    }
}
