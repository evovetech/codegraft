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

import com.laynemobile.api.functions.SimpleSourceTransform_call__P_Subscriber;
import com.laynemobile.api.functions.SimpleSource_call__P_Subscriber;
import com.laynemobile.proxy.AbstractProxyTypeBuilder;
import com.laynemobile.proxy.ProxyType;
import com.laynemobile.proxy.annotations.Generated;
import com.laynemobile.proxy.functions.Action1;
import com.laynemobile.proxy.functions.Func0;

import rx.Observable;
import rx.Subscriber;

@Generated
@SourceProxy
public class SimpleSourceProxyTypeBuilder<T> extends AbstractProxyTypeBuilder<SimpleSource<T>> {
    private final SimpleSource_call__P_Subscriber<T> sourceDef = new SimpleSource_call__P_Subscriber<>();
    private final SourceProxyTypeBuilder<T, NoParams> source = new SourceProxyTypeBuilder<>();

    public SimpleSourceProxyTypeBuilder<T> setSource(SimpleSource_call__P_Subscriber.Action<? extends T> source) {
        this.source.setSource(source);
        return this;
    }

    public SimpleSourceProxyTypeBuilder<T> setSource(SimpleSourceTransform_call__P_Subscriber<T> source) {
        return setSource(this.sourceDef.asFunction(source));
    }

    public SimpleSourceProxyTypeBuilder<T> setSource(Action1<? super Subscriber<? super T>> source) {
        return setSource(new SimpleSourceTransform_call__P_Subscriber<T>(source));
    }

    public SimpleSourceProxyTypeBuilder<T> setSource(Func0<? extends T> source) {
        return setSource(new SimpleSourceTransform_call__P_Subscriber<T>(source));
    }

    public SimpleSourceProxyTypeBuilder<T> setSource(Observable<? extends T> source) {
        return setSource(new SimpleSourceTransform_call__P_Subscriber<T>(source));
    }

    @Override public ProxyType<SimpleSource<T>> buildProxyType() {
        return new SimpleSourceDef<T>().newProxyBuilder()
                .addSuperType(source.buildProxyType())
                .build();
    }
}
