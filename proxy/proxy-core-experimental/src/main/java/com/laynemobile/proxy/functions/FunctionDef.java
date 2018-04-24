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

package com.laynemobile.proxy.functions;

import com.google.common.base.Objects;
import com.laynemobile.proxy.TypeToken;
import com.laynemobile.proxy.functions.transforms.FunctionTransform;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.unmodifiableList;

public class FunctionDef<F extends FunctionTransform<?, R>, R> {
    private final String name;
    private final TypeToken<R> returnType;
    private final List<TypeToken<?>> paramTypes;

    public FunctionDef(FunctionDef<? super F, R> functionDef) {
        this.name = functionDef.name();
        this.returnType = functionDef.returnType();
        this.paramTypes = functionDef.paramTypes();
    }

    public FunctionDef(String name, TypeToken<R> returnType, TypeToken<?>[] paramTypes) {
        List<? extends TypeToken<?>> paramTypesList = Arrays.asList(paramTypes.clone());
        this.name = name;
        this.returnType = returnType;
        this.paramTypes = unmodifiableList(paramTypesList);
    }

    public final String name() {
        return name;
    }

    public final TypeToken<R> returnType() {
        return returnType;
    }

    public final List<TypeToken<?>> paramTypes() {
        return paramTypes;
    }

    public ProxyFunction<F, R> asFunction(F transform) {
        return new ProxyFunction<>(this, transform);
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FunctionDef)) return false;
        FunctionDef<?, ?> that = (FunctionDef<?, ?>) o;
        return Objects.equal(name, that.name) &&
                Objects.equal(returnType, that.returnType) &&
                Objects.equal(paramTypes, that.paramTypes);
    }

    @Override public int hashCode() {
        return Objects.hashCode(name, returnType, paramTypes);
    }
}
