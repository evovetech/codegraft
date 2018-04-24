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
import com.laynemobile.api.Retrofittable;
import com.laynemobile.proxy.annotations.Generated;
import com.laynemobile.proxy.functions.Action2;
import com.laynemobile.proxy.functions.Action3;
import com.laynemobile.proxy.functions.Func0;
import com.laynemobile.proxy.functions.Func1;
import com.laynemobile.proxy.functions.Func2;

import rx.Subscriber;

@Generated
public class RetrofitSourceProxy_call__P_Subscriber<T, P extends Params, S> extends SourceProxy_call__P_Subscriber<T, P> {
    public RetrofitSourceProxy_call__P_Subscriber(final Action3<S, P, Subscriber<? super T>> source,
            final Func0<? extends Retrofittable<? extends S>> retrofittable) {
        super(new Action2<P, Subscriber<? super T>>() {
            @Override public void call(P p, Subscriber<? super T> subscriber) {
                S s = retrofittable.call().getService();
                source.call(s, p, subscriber);
            }
        });
    }

    public RetrofitSourceProxy_call__P_Subscriber(final Func2<S, P, T> source,
            final Func0<? extends Retrofittable<? extends S>> retrofittable) {
        super(new Func1<P, T>() {
            @Override public T call(P p) {
                S s = retrofittable.call().getService();
                return source.call(s, p);
            }
        });
    }
}
