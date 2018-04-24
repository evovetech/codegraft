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
import com.laynemobile.proxy.functions.transforms.ProxyActionTransform;

public class ProxyActionDef<P, A extends ProxyActionTransform<P, ?>> extends ProxyFunctionDef<P, A, Void> {
    private static final TypeToken<Void> VOID_TYPE = TypeToken.get(Void.TYPE);

    public ProxyActionDef(ProxyActionDef<P, A> actionDef) {
        super(actionDef);
    }

    public ProxyActionDef(String name, TypeToken<?>[] paramTypes) {
        super(name, VOID_TYPE, paramTypes);
    }

    @Override public ProxyAction2<P, A> asFunction(A transform) {
        return new ProxyAction2<>(this, transform);
    }
}
