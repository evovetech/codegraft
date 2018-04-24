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

import com.laynemobile.api.NoParams;
import com.laynemobile.proxy.TypeToken;
import com.laynemobile.proxy.functions.transforms.Action2Transform;

import rx.Subscriber;

public class SimpleSource_call__P_Subscriber<T> extends Source_call__P_Subscriber<T, NoParams> {
    public SimpleSource_call__P_Subscriber(TypeToken<NoParams> t1, TypeToken<Subscriber<? super T>> t2) {
        super(t1, t2);
    }

    public SimpleSource_call__P_Subscriber() {}

    @Override public Action<T> asFunction(Action2Transform<NoParams, Subscriber<? super T>> transform) {
        return new Action<>(this, transform);
    }

    public static class Action<T> extends Source_call__P_Subscriber.Action<T, NoParams> {
        protected Action(SimpleSource_call__P_Subscriber<T> actionDef,
                Action2Transform<NoParams, Subscriber<? super T>> action) {
            super(actionDef, action);
        }
    }
}
