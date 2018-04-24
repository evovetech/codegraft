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

import com.laynemobile.api.functions.SourceTransform_call__P_Subscriber;
import com.laynemobile.api.functions.Source_call__P_Subscriber;
import com.laynemobile.proxy.AbstractProxyTypeBuilder;
import com.laynemobile.proxy.ProxyType;
import com.laynemobile.proxy.annotations.Generated;
import com.laynemobile.proxy.functions.Action1;
import com.laynemobile.proxy.functions.Action2;
import com.laynemobile.proxy.functions.Func0;
import com.laynemobile.proxy.functions.Func1;

import rx.Observable;
import rx.Subscriber;

@Generated
public class SourceProxyTypeBuilder<T, P extends Params> extends AbstractProxyTypeBuilder<Source<T, P>> {
    private Source_call__P_Subscriber<T, P> sourceDef = new Source_call__P_Subscriber<>();
    private Source_call__P_Subscriber.Action<? extends T, ? extends P> source;

    public SourceProxyTypeBuilder<T, P> setSource(Source_call__P_Subscriber.Action<? extends T, ? extends P> source) {
        this.source = source;
        return this;
    }

    public SourceProxyTypeBuilder<T, P> setSource(SourceTransform_call__P_Subscriber<T, P> source) {
        return setSource(this.sourceDef.asFunction(source));
    }

    public SourceProxyTypeBuilder<T, P> setSource(Action2<? super P, ? super Subscriber<? super T>> source) {
        return setSource(new SourceTransform_call__P_Subscriber<T, P>(source));
    }

    public SourceProxyTypeBuilder<T, P> setSource(Action1<? super Subscriber<? super T>> source) {
        return setSource(new SourceTransform_call__P_Subscriber<T, P>(source));
    }

    public SourceProxyTypeBuilder<T, P> setSource(Func1<? super P, ? extends T> source) {
        return setSource(new SourceTransform_call__P_Subscriber<T, P>(source));
    }

    public SourceProxyTypeBuilder<T, P> setSource(Func0<? extends T> source) {
        return setSource(new SourceTransform_call__P_Subscriber<T, P>(source));
    }

    public SourceProxyTypeBuilder<T, P> setSource(Observable<? extends T> source) {
        return setSource(new SourceTransform_call__P_Subscriber<T, P>(source));
    }

    @Override public ProxyType<Source<T, P>> buildProxyType() {
        return new SourceDef<T, P>().newProxyBuilder()
                .addFunction(source)
                .build();
    }
}
