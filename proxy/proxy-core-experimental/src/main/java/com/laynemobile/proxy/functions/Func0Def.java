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

package com.laynemobile.proxy.functions;

import com.laynemobile.proxy.TypeToken;
import com.laynemobile.proxy.functions.transforms.Func0Transform;

public class Func0Def<R> extends FunctionDef<Func0Transform<R>, R> {
    public Func0Def(String name, TypeToken<R> returnType) {
        super(name, returnType, new TypeToken<?>[0]);
    }

    @Override public Function<R> asFunction(Func0Transform<R> transform) {
        return new Function<>(this, transform);
    }

    public static class Function<R> extends ProxyFunction<Func0Transform<R>, R> {
        protected Function(Func0Def<R> functionDef, Func0Transform<R> function) {
            super(functionDef, function);
        }
    }
}
