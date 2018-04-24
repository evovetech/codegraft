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

package com.laynemobile.proxy.functions.transforms;

import com.laynemobile.proxy.functions.Func0;
import com.laynemobile.proxy.functions.Func1;

public class ProxyFunc0Transform<P, R>
        extends ProxyFunctionTransform<P, Func1<? super P, ? extends R>, R>
        implements Func1<P, R> {

    public ProxyFunc0Transform(Func1<? super P, ? extends R> function) {
        super(function);
    }

    public ProxyFunc0Transform(ProxyFunc0Transform<? super P, ? extends R> function) {
        super(function.function);
    }

    public ProxyFunc0Transform(final Func0<? extends R> function) {
        super(new Func1<P, R>() {
            @Override public R call(P p) {
                return function.call();
            }
        });
    }

    public ProxyFunc0Transform(final R value) {
        super(new Func1<P, R>() {
            @Override public R call(P p) {
                return value;
            }
        });
    }

    @Override public final R call(P proxy) {
        return function.call(proxy);
    }

    @Override public final R call(P proxy, Object... args) {
        if (args.length != 0) {
            throw new RuntimeException("Func0 expecting 0 arguments.");
        }
        return function.call(proxy);
    }
}
