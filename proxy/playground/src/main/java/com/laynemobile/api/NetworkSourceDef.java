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

import com.laynemobile.api.functions.NetworkSource_networkChecker;
import com.laynemobile.proxy.AbstractProxyDef;
import com.laynemobile.proxy.TypeDef;

public class NetworkSourceDef<T, P extends Params> extends AbstractProxyDef<NetworkSource<T, P>> {
    final NetworkSource_networkChecker networkChecker = new NetworkSource_networkChecker();
    final TypeDef<NetworkSource<T, P>> typeDef = new TypeDef.Builder<NetworkSource<T, P>>() {}
            .addSuperType(new SourceDef<T, P>().typeDef())
            .addFunction(networkChecker)
            .build();

    @Override public TypeDef<NetworkSource<T, P>> typeDef() {
        return typeDef;
    }
}
