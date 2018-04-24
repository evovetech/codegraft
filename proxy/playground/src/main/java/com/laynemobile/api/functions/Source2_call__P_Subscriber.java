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

package com.laynemobile.api.functions;

import com.laynemobile.api.Params;
import com.laynemobile.api.Source;
import com.laynemobile.proxy.TypeToken;
import com.laynemobile.proxy.functions.ProxyAction2Def;
import com.laynemobile.proxy.functions.transforms.ProxyAction2Transform;

import rx.Subscriber;

public class Source2_call__P_Subscriber<T, P extends Params> extends ProxyAction2Def<Source<T, P>, P, Subscriber<? super T>> {
    public Source2_call__P_Subscriber(TypeToken<P> t1, TypeToken<Subscriber<? super T>> t2) {
        super("call", t1, t2);
    }

    public Source2_call__P_Subscriber() {
        this(new TypeToken<P>() {}, new TypeToken<Subscriber<? super T>>() {});
    }

    @Override public Action<T, P> asFunction(ProxyAction2Transform<Source<T, P>, P, Subscriber<? super T>> transform) {
        return new Action<>(this, transform);
    }

    public static class Action<T, P extends Params> extends ProxyAction2Def.Action<Source<T, P>, P, Subscriber<? super T>> {
        public Action(Source2_call__P_Subscriber<T, P> actionDef,
                ProxyAction2Transform<Source<T, P>, P, Subscriber<? super T>> action) {
            super(actionDef, action);
        }
    }
}
