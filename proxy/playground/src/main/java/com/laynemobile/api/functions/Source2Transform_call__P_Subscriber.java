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
import com.laynemobile.api.Source;
import com.laynemobile.proxy.annotations.Generated;
import com.laynemobile.proxy.functions.Action0;
import com.laynemobile.proxy.functions.Action1;
import com.laynemobile.proxy.functions.Action2;
import com.laynemobile.proxy.functions.Action3;
import com.laynemobile.proxy.functions.Func0;
import com.laynemobile.proxy.functions.Func1;
import com.laynemobile.proxy.functions.transforms.ProxyAction2Transform;

import rx.Observable;
import rx.Subscriber;

// TODO: annotate
// @FunctionTransformer(SourceDef_call__P_Subscriber.Transform.class)
@Generated
public class Source2Transform_call__P_Subscriber<T, P extends Params> extends ProxyAction2Transform<Source<T, P>, P, Subscriber<? super T>> {
    public Source2Transform_call__P_Subscriber(
            Action3<? super Source<T, P>, ? super P, ? super Subscriber<? super T>> action) {
        super(action);
    }

    public Source2Transform_call__P_Subscriber(Action2<? super P, ? super Subscriber<? super T>> action) {
        super(action);
    }

    public Source2Transform_call__P_Subscriber(Action0 action) {
        super(action);
    }

    public Source2Transform_call__P_Subscriber(final Action1<? super Subscriber<? super T>> source) {
        super(new Action2<P, Subscriber<? super T>>() {
            @Override public void call(P p, Subscriber<? super T> subscriber) {
                source.call(subscriber);
            }
        });
    }

    public Source2Transform_call__P_Subscriber(final Func1<? super P, ? extends T> source) {
        super(new Action2<P, Subscriber<? super T>>() {
            @Override public void call(P p, Subscriber<? super T> subscriber) {
                try {
                    T t = source.call(p);
                    subscriber.onNext(t);
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public Source2Transform_call__P_Subscriber(final Func0<? extends T> source) {
        this(new Func1<P, T>() {
            @Override public T call(P p) {
                return source.call();
            }
        });
    }

    public Source2Transform_call__P_Subscriber(final Observable<? extends T> source) {
        super(new Action2<P, Subscriber<? super T>>() {
            @Override public void call(P p, final Subscriber<? super T> child) {
                source.unsafeSubscribe(new Subscriber<T>(child) {
                    @Override public void onCompleted() {
                        if (!child.isUnsubscribed()) {
                            child.onCompleted();
                        }
                    }

                    @Override public void onError(Throwable e) {
                        if (!child.isUnsubscribed()) {
                            child.onError(e);
                        }
                    }

                    @Override public void onNext(T t) {
                        if (!child.isUnsubscribed()) {
                            child.onNext(t);
                        }
                    }
                });
            }
        });
    }
}
