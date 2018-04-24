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

import com.laynemobile.proxy.annotations.GenerateProxyType;
import com.laynemobile.proxy.annotations.ProxyTypeFunction;

@GenerateProxyType(parent = true)
public interface Retrofittable<S> {
    @ProxyTypeFunction("service")
    S getService();

//    final class Builder<S> implements com.laynemobile.proxy.Builder<Retrofittable<S>> {
//        private Class<S> serviceType;
//        private Func0<RestAdapter> restAdapter;
//
//        public Builder<S> setServiceType(Class<S> serviceType) {
//            this.serviceType = serviceType;
//            return this;
//        }
//
//        public Builder<S> setRestAdapter(RestAdapter restAdapter) {
//            return setRestAdapterInternal(this, restAdapter);
//        }
//
//        public Builder<S> setRestAdapter(Func0<RestAdapter> restAdapter) {
//            this.restAdapter = restAdapter;
//            return this;
//        }
//
//        @Override public Retrofittable<S> build() {
//            return new RetrofittableImpl<S>(this);
//        }
//
//        private static <S> Builder<S> setRestAdapterInternal(Builder<S> builder, final RestAdapter restAdapter) {
//            // Create anonymous inner class in static context to avoid holding Builder instance in memory
//            builder.restAdapter = new Func0<RestAdapter>() {
//                @Override public RestAdapter call() {
//                    return restAdapter;
//                }
//            };
//            return builder;
//        }
//
//        private static final class RetrofittableImpl<S> implements Retrofittable<S> {
//            private static final Func0<RestAdapter> DEFAULT = new Func0<RestAdapter>() {
//                private final AtomicReference<RestAdapter> restAdapter = new AtomicReference<RestAdapter>();
//
//                @Override public RestAdapter call() {
//                    RestAdapter restAdapter = this.restAdapter.get();
//                    if (restAdapter == null) {
//                        restAdapter = RetrofitStashModule.instance()
//                                .getRetrofitHook()
//                                .defaultRestAdapter();
//                        if (restAdapter == null) {
//                            throw new IllegalStateException("default rest adapter is null. " +
//                                    "Must be configured in StashModule.registerRetrofitHook");
//                        }
//                        this.restAdapter.compareAndSet(null, restAdapter);
//                        restAdapter = this.restAdapter.get();
//                    }
//                    return restAdapter;
//                }
//            };
//
//            private final Class<S> serviceType;
//            private final Func0<RestAdapter> restAdapter;
//            private final AtomicReference<S> service = new AtomicReference<S>();
//
//            private RetrofittableImpl(Builder<S> builder) {
//                this.serviceType = builder.serviceType;
//                if (serviceType == null) {
//                    throw new IllegalStateException("serviceType must not be null");
//                }
//                Func0<RestAdapter> restAdapter = builder.restAdapter;
//                if (restAdapter == null) {
//                    restAdapter = DEFAULT;
//                }
//                this.restAdapter = restAdapter;
//            }
//
//            @Override public S getService() {
//                S service = this.service.get();
//                if (service == null) {
//                    RestAdapter restAdapter = this.restAdapter.call();
//                    this.service.compareAndSet(null, restAdapter.create(serviceType));
//                    service = this.service.get();
//                }
//                return service;
//            }
//        }
//    }
}
