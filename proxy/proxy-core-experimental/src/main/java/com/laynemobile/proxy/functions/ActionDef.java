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
import com.laynemobile.proxy.functions.transforms.ActionTransform;

public class ActionDef<A extends ActionTransform<?>> extends FunctionDef<A, Void> {
    private static final TypeToken<Void> VOID_TYPE = TypeToken.get(Void.TYPE);

    public ActionDef(ActionDef<? super A> actionDef) {
        super(actionDef);
    }

    public ActionDef(String name, TypeToken<?>[] paramTypes) {
        super(name, VOID_TYPE, paramTypes);
    }

    @Override public ProxyAction<A> asFunction(A transform) {
        return new ProxyAction<>(this, transform);
    }
}
