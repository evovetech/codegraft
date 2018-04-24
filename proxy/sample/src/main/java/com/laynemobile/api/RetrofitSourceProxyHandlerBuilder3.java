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

import com.laynemobile.api.functions.RetrofitSourceProxy_call__P_Subscriber;
import com.laynemobile.api.functions.RetrofitSourceProxy_getRetrofittable;
import com.laynemobile.api.functions.RetrofitSourceProxy_networkChecker;
import com.laynemobile.proxy.NamedMethodHandler;
import com.laynemobile.proxy.ProxyBuilder;
import com.laynemobile.proxy.ProxyHandler;
import com.laynemobile.proxy.TypeToken;
import com.laynemobile.proxy.annotations.Generated;
import com.laynemobile.proxy.functions.Action3;
import com.laynemobile.proxy.functions.Func0;
import com.laynemobile.proxy.functions.Func2;

import rx.Subscriber;

@Generated
public class RetrofitSourceProxyHandlerBuilder3<T, P extends Params, S> {
    public SourceBuilder<T, P, S> setRetrofittable(
            RetrofitSourceProxy_getRetrofittable<? extends T, ? extends P, ? extends S> retrofittable) {
        return new SourceBuilder<>(retrofittable);
    }

    public SourceBuilder<T, P, S> setRetrofittable(Func0<Retrofittable<S>> retrofittable) {
        return new SourceBuilder<>(new RetrofitSourceProxy_getRetrofittable<T, P, S>(retrofittable));
    }

    public static class SourceBuilder<T, P extends Params, S> {
        private final RetrofitSourceProxy_getRetrofittable<? extends T, ? extends P, ? extends S> retrofittable;
        private final SourceProxyHandlerBuilder<T, P> source = new SourceProxyHandlerBuilder<>();

        private SourceBuilder(
                RetrofitSourceProxy_getRetrofittable<? extends T, ? extends P, ? extends S> retrofittable) {
            this.retrofittable = retrofittable;
        }

        public NetworkSourceBuilder<T, P, S> setSource(Action3<S, P, Subscriber<? super T>> source) {
            this.source.setSource(new RetrofitSourceProxy_call__P_Subscriber<>(source, retrofittable.function()));
            return next();
        }

        public NetworkSourceBuilder<T, P, S> setSource(Func2<S, P, T> source) {
            this.source.setSource(new RetrofitSourceProxy_call__P_Subscriber<>(source, retrofittable.function()));
            return next();
        }

        private NetworkSourceBuilder<T, P, S> next() {
            return new NetworkSourceBuilder<>(source.proxyHandler(), retrofittable.handler());
        }
    }

    public static class NetworkSourceBuilder<T, P extends Params, S> {
        private final ProxyHandler<Source<T, P>> sourceHandler;
        private final NamedMethodHandler retrofittableHandler;
        private final NetworkSourceProxyHandlerBuilder<T, P> networkSource = new NetworkSourceProxyHandlerBuilder<>();

        private NetworkSourceBuilder(ProxyHandler<Source<T, P>> sourceHandler,
                NamedMethodHandler retrofittableHandler) {
            this.sourceHandler = sourceHandler;
            this.retrofittableHandler = retrofittableHandler;
        }

        public Completer<T, P, S> setNetworkChecker(
                RetrofitSourceProxy_networkChecker<? extends T, ? extends P, ? extends S> networkChecker) {
            this.networkSource.setNetworkChecker(networkChecker);
            return next();
        }

        public Completer<T, P, S> setNetworkChecker(Func0<NetworkChecker> networkChecker) {
            this.networkSource.setNetworkChecker(new RetrofitSourceProxy_networkChecker<T, P, S>(networkChecker));
            return next();
        }

        public Completer<T, P, S> setNetworkChecker(NetworkChecker networkChecker) {
            this.networkSource.setNetworkChecker(new RetrofitSourceProxy_networkChecker<T, P, S>(networkChecker));
            return next();
        }

        public Completer<T, P, S> setNetworkChecker() {
            this.networkSource.setNetworkChecker(new RetrofitSourceProxy_networkChecker<T, P, S>());
            return next();
        }

        private Completer<T, P, S> next() {
            return new Completer<>(handler());
        }

        private ProxyHandler<RetrofitSource<T, P, S>> handler() {
            return ProxyHandler.builder(new TypeToken<RetrofitSource<T, P, S>>() {})
                    .addParent(sourceHandler)
                    .addParent(networkSource.proxyHandler())
                    .handle(retrofittableHandler)
                    .build();
        }
    }

    public static class Completer<T, P extends Params, S> extends ProxyBuilder<RetrofitSource<T, P, S>> {
        private final ProxyHandler<RetrofitSource<T, P, S>> handler;

        private Completer(ProxyHandler<RetrofitSource<T, P, S>> handler) {
            super(handler);
            this.handler = handler;
        }

        public ProxyHandler<RetrofitSource<T, P, S>> handler() {
            return handler;
        }
    }
}
