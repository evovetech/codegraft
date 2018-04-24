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

import com.laynemobile.api.functions.Source2Transform_call__P_Subscriber;
import com.laynemobile.api.functions.Source2_call__P_Subscriber;
import com.laynemobile.proxy.AbstractProxyType2Builder;
import com.laynemobile.proxy.ProxyType2;
import com.laynemobile.proxy.annotations.Generated;
import com.laynemobile.proxy.functions.Action1;
import com.laynemobile.proxy.functions.Action2;
import com.laynemobile.proxy.functions.Func0;
import com.laynemobile.proxy.functions.Func1;

import rx.Observable;
import rx.Subscriber;

@Generated
public class Source2ProxyTypeBuilder<T, P extends Params> extends AbstractProxyType2Builder<Source<T, P>> {
    private final Source2Def<T, P> def = new Source2Def<>();
    private Source2_call__P_Subscriber.Action<T, P> source;

    public Source2ProxyTypeBuilder<T, P> setSource(Source2_call__P_Subscriber.Action<T, P> source) {
        this.source = source;
        return this;
    }

    public Source2ProxyTypeBuilder<T, P> setSource(Source2Transform_call__P_Subscriber<T, P> source) {
        return setSource(def.call__p_subscriber().asFunction(source));
    }

    public Source2ProxyTypeBuilder<T, P> setSource(Action2<? super P, ? super Subscriber<? super T>> source) {
        return setSource(new Source2Transform_call__P_Subscriber<T, P>(source));
    }

    public Source2ProxyTypeBuilder<T, P> setSource(Action1<? super Subscriber<? super T>> source) {
        return setSource(new Source2Transform_call__P_Subscriber<T, P>(source));
    }

    public Source2ProxyTypeBuilder<T, P> setSource(Func1<? super P, ? extends T> source) {
        return setSource(new Source2Transform_call__P_Subscriber<T, P>(source));
    }

    public Source2ProxyTypeBuilder<T, P> setSource(Func0<? extends T> source) {
        return setSource(new Source2Transform_call__P_Subscriber<T, P>(source));
    }

    public Source2ProxyTypeBuilder<T, P> setSource(Observable<? extends T> source) {
        return setSource(new Source2Transform_call__P_Subscriber<T, P>(source));
    }

    @Override public ProxyType2<Source<T, P>> buildProxyType() {
        return def.newProxyBuilder()
                .addFunction(source)
                .build();
    }
}
