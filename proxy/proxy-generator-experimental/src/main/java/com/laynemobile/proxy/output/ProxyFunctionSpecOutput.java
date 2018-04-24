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

package com.laynemobile.proxy.output;

import com.squareup.javapoet.TypeSpec;

public class ProxyFunctionSpecOutput extends AbstractTypeElementOutput<ProxyFunctionSpecOutputStub> {
    ProxyFunctionSpecOutput(ProxyFunctionSpecOutputStub source, TypeSpec typeSpec, boolean didWrite) {
        super(source, typeSpec, didWrite);
    }

    // TODO: output could be Standalone Transformer
//    @Override public boolean hasOutput() {
//        return true;
//    }
//
//    @Override public ProxyFunctionSpecOutputStub outputStub(Env env) {
//        return new ProxyFunctionSpecOutputStub(source(), env);
//    }
}
