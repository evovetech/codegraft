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

import com.laynemobile.api.functions.Source2_call__P_Subscriber;
import com.laynemobile.proxy.AbstractProxyDef2;
import com.laynemobile.proxy.TypeDef2;

public class Source2Def<T, P extends Params> extends AbstractProxyDef2<Source<T, P>> {
    private final Source2_call__P_Subscriber<T, P> call__p_subscriber = new Source2_call__P_Subscriber<>();
    private final TypeDef2<Source<T, P>> typeDef = new TypeDef2.Builder<Source<T, P>>() {}
            .addFunction(call__p_subscriber)
            .build();

    public Source2_call__P_Subscriber<T, P> call__p_subscriber() {
        return call__p_subscriber;
    }

    @Override public TypeDef2<Source<T, P>> typeDef() {
        return typeDef;
    }
}
