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

import com.laynemobile.api.functions.NetworkSourceProxy_networkChecker;
import com.laynemobile.api.functions.RetrofitSourceProxy_getRetrofittable;
import com.laynemobile.api.functions.SourceProxy_call__P_Subscriber;
import com.laynemobile.proxy.AbstractProxyHandlerBuilder;
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
@SourceProxy
public class RetrofitSourceProxyHandlerBuilder2<T, P extends Params, S> extends AbstractProxyHandlerBuilder<RetrofitSource<T, P, S>> {
    private final SourceProxyHandlerBuilder<T, P> source = new SourceProxyHandlerBuilder<>();
    private final NetworkSourceProxyHandlerBuilder<T, P> networkSource = new NetworkSourceProxyHandlerBuilder<>();
    private RetrofitSourceProxy_getRetrofittable<? extends T, ? extends P, ? extends S> getRetrofittable;

    public RetrofitSourceProxyHandlerBuilder2<T, P, S> setSource(
            SourceProxy_call__P_Subscriber<? extends T, ? extends P> source) {
        this.source.setSource(source);
        return this;
    }

    public RetrofitSourceProxyHandlerBuilder2<T, P, S> setSource(Action2<P, Subscriber<? super T>> source) {
        this.source.setSource(source);
        return this;
    }

    public RetrofitSourceProxyHandlerBuilder2<T, P, S> setSource(
            Action1<Subscriber<? super T>> source) {
        this.source.setSource(source);
        return this;
    }

    public RetrofitSourceProxyHandlerBuilder2<T, P, S> setSource(Func1<P, T> source) {
        this.source.setSource(source);
        return this;
    }

    public RetrofitSourceProxyHandlerBuilder2<T, P, S> setSource(Func0<T> source) {
        this.source.setSource(source);
        return this;
    }

    public RetrofitSourceProxyHandlerBuilder2<T, P, S> setSource(Observable<T> source) {
        this.source.setSource(source);
        return this;
    }

    public RetrofitSourceProxyHandlerBuilder2<T, P, S> setNetworkChecker(
            NetworkSourceProxy_networkChecker<? extends T, ? extends P> networkChecker) {
        networkSource.setNetworkChecker(networkChecker);
        return this;
    }

    public RetrofitSourceProxyHandlerBuilder2<T, P, S> setNetworkChecker(
            Func0<NetworkChecker> networkChecker) {
        networkSource.setNetworkChecker(networkChecker);
        return this;
    }

    public RetrofitSourceProxyHandlerBuilder2<T, P, S> setNetworkChecker(NetworkChecker networkChecker) {
        networkSource.setNetworkChecker(networkChecker);
        return this;
    }

    public RetrofitSourceProxyHandlerBuilder2<T, P, S> setNetworkChecker() {
        networkSource.setNetworkChecker();
        return this;
    }

    public RetrofitSourceProxyHandlerBuilder2<T, P, S> setGetRetrofittable(
            RetrofitSourceProxy_getRetrofittable<? extends T, ? extends P, ? extends S> getRetrofittable) {
        this.getRetrofittable = getRetrofittable;
        return this;
    }

    public RetrofitSourceProxyHandlerBuilder2<T, P, S> setGetRetrofittable(Func0<Retrofittable<S>> getRetrofittable) {
        this.getRetrofittable = new RetrofitSourceProxy_getRetrofittable<T, P, S>(getRetrofittable);
        return this;
    }

    @Override
    public ProxyHandler<RetrofitSource<T, P, S>> proxyHandler() {
        return ProxyHandler.builder(new TypeToken<RetrofitSource<T, P, S>>() {})
                .addParent(source.proxyHandler())
                .addParent(networkSource.proxyHandler())
                .handle(handler(getRetrofittable))
                .build();
    }
}
