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

final class ProxyObject_toString extends Func0Def<String> {
    ProxyObject_toString() {
        super("toString", TypeToken.get(String.class));
    }

    @Override public Function asFunction(Func0Transform<String> transform) {
        return new Function(this, transform);
    }

    static final class Function extends Func0Def.Function<String> {
        private Function(ProxyObject_toString functionDef, Func0Transform<String> function) {
            super(functionDef, function);
        }
    }
}
