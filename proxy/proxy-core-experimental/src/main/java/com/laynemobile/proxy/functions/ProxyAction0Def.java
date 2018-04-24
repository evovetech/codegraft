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
import com.laynemobile.proxy.functions.transforms.ProxyAction0Transform;

public class ProxyAction0Def<P> extends ProxyActionDef<P, ProxyAction0Transform<P>> {
    public ProxyAction0Def(String name) {
        super(name, new TypeToken<?>[0]);
    }

    @Override public Action<P> asFunction(ProxyAction0Transform<P> transform) {
        return new Action<>(this, transform);
    }

    public static class Action<P> extends ProxyAction2<P, ProxyAction0Transform<P>> {
        protected Action(ProxyAction0Def<P> actionDef, ProxyAction0Transform<P> action) {
            super(actionDef, action);
        }
    }
}
