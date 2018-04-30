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

import com.laynemobile.api.NetworkChecker;
import com.laynemobile.proxy.TypeToken;
import com.laynemobile.proxy.annotations.Generated;
import com.laynemobile.proxy.functions.Func0Def;
import com.laynemobile.proxy.functions.transforms.Func0Transform;

@Generated
public class NetworkSource_networkChecker extends Func0Def<NetworkChecker> {
    public NetworkSource_networkChecker() {
        super("networkChecker", TypeToken.get(NetworkChecker.class));
    }

    @Override public Function asFunction(Func0Transform<NetworkChecker> transform) {
        return new Function(this, transform);
    }

    public static class Function extends Func0Def.Function<NetworkChecker> {
        protected Function(NetworkSource_networkChecker functionDef, Func0Transform<NetworkChecker> function) {
            super(functionDef, function);
        }
    }
}