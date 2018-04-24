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

package com.laynemobile.api;

import com.laynemobile.api.functions.SourceProxy_call__P_Subscriber;
import com.laynemobile.proxy.AbstractProxyHandlerBuilder;
import com.laynemobile.proxy.ProxyCompleter;
import com.laynemobile.proxy.ProxyHandler;
import com.laynemobile.proxy.TypeToken;
import com.laynemobile.proxy.annotations.Generated;
import com.laynemobile.proxy.functions.Action1;
import com.laynemobile.proxy.functions.Action2;
import com.laynemobile.proxy.functions.Func0;
import com.laynemobile.proxy.functions.Func1;

import rx.Observable;
import rx.Subscriber;

@Generated
public class SourceProxyHandlerBuilder2<T, P extends Params> {
    private SourceProxy_call__P_Subscriber<? extends T, ? extends P> source;

    public Completer<T, P> setSource(SourceProxy_call__P_Subscriber<? extends T, ? extends P> source) {
        this.source = source;
        return next();
    }

    public Completer<T, P> setSource(Action2<P, Subscriber<? super T>> source) {
        this.source = new SourceProxy_call__P_Subscriber<T, P>(source);
        return next();
    }

    public Completer<T, P> setSource(Action1<Subscriber<? super T>> source) {
        this.source = new SourceProxy_call__P_Subscriber<T, P>(source);
        return next();
    }

    public Completer<T, P> setSource(Func1<P, T> source) {
        this.source = new SourceProxy_call__P_Subscriber<T, P>(source);
        return next();
    }

    public Completer<T, P> setSource(Func0<T> source) {
        this.source = new SourceProxy_call__P_Subscriber<T, P>(source);
        return next();
    }

    public Completer<T, P> setSource(Observable<T> source) {
        this.source = new SourceProxy_call__P_Subscriber<T, P>(source);
        return next();
    }

    private ProxyHandler<Source<T, P>> handler() {
        return ProxyHandler.builder(new TypeToken<Source<T, P>>() {})
                .handle(AbstractProxyHandlerBuilder.handler(source))
                .build();
    }

    private Completer<T, P> next() {
        return new Completer<>(handler());
    }

    public static final class Completer<T, P extends Params> extends ProxyCompleter<Source<T, P>> {
        private Completer(ProxyHandler<Source<T, P>> handler) {
            super(handler);
        }
    }
}
