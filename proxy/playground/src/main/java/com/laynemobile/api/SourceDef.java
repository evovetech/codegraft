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

import com.laynemobile.api.functions.Source_call__P_Subscriber;
import com.laynemobile.proxy.AbstractProxyDef;
import com.laynemobile.proxy.TypeDef;

public class SourceDef<T, P extends Params> extends AbstractProxyDef<Source<T, P>> {
    final Source_call__P_Subscriber<T, P> call__p_subscriber = new Source_call__P_Subscriber<>();
    final TypeDef<Source<T, P>> typeDef = new TypeDef.Builder<Source<T, P>>() {}
            .addFunction(call__p_subscriber)
            .build();

    @Override public TypeDef<Source<T, P>> typeDef() {
        return typeDef;
    }
}
