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

import com.laynemobile.proxy.functions.Func0;
import com.laynemobile.proxy.functions.Func2;
import com.laynemobile.proxy.functions.transforms.Func2Transform;

public class ProxyFunc1Transform<T1 extends ProxyState<?, ?>, T2, R> extends Func2Transform<T1, T2, R> {
    public ProxyFunc1Transform(Func2<? super T1, ? super T2, ? extends R> function) {
        super(function);
    }

    public ProxyFunc1Transform(Func0<? extends R> function) {
        super(function);
    }

    public ProxyFunc1Transform(R value) {
        super(value);
    }
}
