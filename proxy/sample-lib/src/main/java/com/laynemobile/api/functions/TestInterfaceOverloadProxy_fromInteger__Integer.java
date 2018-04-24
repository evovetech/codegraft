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

import com.laynemobile.api.functions.parent.AbstractTestInterfaceOverloadProxy_fromInteger__Integer;
import com.laynemobile.proxy.NamedMethodHandler;
import com.laynemobile.proxy.annotations.Generated;
import com.laynemobile.proxy.functions.Func1;
import com.laynemobile.proxy.functions.FuncN;
import com.laynemobile.proxy.functions.FuncNHandler;
import com.laynemobile.proxy.functions.Functions;

@Generated
public class TestInterfaceOverloadProxy_fromInteger__Integer<T, R> extends AbstractTestInterfaceOverloadProxy_fromInteger__Integer<T, R> {
    public TestInterfaceOverloadProxy_fromInteger__Integer(Func1<Integer, String> fromInteger) {
        super(fromInteger);
    }

    @Override public NamedMethodHandler handler() {
        return new NamedMethodHandler.Builder()
                .setName("fromInteger")
                .setMethodHandler(new FuncNHandler(toFuncN(function()), paramTypes))
                .build();
    }

    private static FuncN<String> toFuncN(Func1<Integer, String> fromInteger) {
        return Functions.fromFunc(fromInteger);
    }
}
