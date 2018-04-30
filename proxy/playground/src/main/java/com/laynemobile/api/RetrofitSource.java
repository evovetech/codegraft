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

import rx.Subscriber;

//@GenerateProxyType(replaces = Source.class)
public interface RetrofitSource<T, P extends Params, S> extends NetworkSource<T, P> {
    @ProxyTypeFunction("retrofittable")
    Retrofittable<S> getRetrofittable();

    @ProxyTypeFunction(value = "source")
    @Override void call(P p, Subscriber<? super T> subscriber);

    @ProxyTypeFunction("networkChecker")
    @Override NetworkChecker networkChecker();
}