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

package com.laynemobile.proxy.functions.transforms;

import com.laynemobile.proxy.functions.Action0;
import com.laynemobile.proxy.functions.Action1;
import com.laynemobile.proxy.functions.Actions;

public class ProxyAction0Transform<P>
        extends ProxyActionTransform<P, Action1<? super P>>
        implements Action1<P> {
    private static final ProxyAction0Transform EMPTY = new ProxyAction0Transform();

    public ProxyAction0Transform() {
        super(Actions.empty());
    }

    public ProxyAction0Transform(final Action0 action) {
        super(new Action1<P>() {
            @Override public void call(P p) {
                action.call();
            }
        });
    }

    public ProxyAction0Transform(Action1<? super P> action) {
        super(action);
    }

    public ProxyAction0Transform(ProxyAction0Transform<? super P> action) {
        super(action.function);
    }

    public static final <P> ProxyAction0Transform<P> empty() {
        return (ProxyAction0Transform<P>) EMPTY;
    }

    @Override protected final void invoke(P proxy, Object... args) {
        if (args.length != 0) {
            throw new RuntimeException("Action0 expecting 0 arguments.");
        }
        function.call(proxy);
    }

    @Override public final void call(P proxy) {
        function.call(proxy);
    }
}
