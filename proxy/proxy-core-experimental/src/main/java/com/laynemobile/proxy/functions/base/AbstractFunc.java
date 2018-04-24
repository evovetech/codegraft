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

package com.laynemobile.proxy.functions.base;

import com.laynemobile.proxy.TypeToken;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

abstract class AbstractFunc<R> extends AbstractFunction<R> {
    private final TypeToken<R> returnType;
    private final List<TypeToken<?>> paramTypes;

    @SuppressWarnings("unchecked") AbstractFunc() {
        Class<?> clazz = getClass();
        Class<?> superClass = clazz.getSuperclass();
        if (AbstractFunc.class.equals(superClass)) {
            throw new IllegalStateException("must not use this class directly");
        }
        superClass = superClass.getSuperclass();
        if (!AbstractFunc.class.equals(superClass)) {
            throw new IllegalStateException("Cannot subclass " + superClass);
        }
        TypeToken<?>[] typeArgs = TypeToken.getTypeParameters(clazz);
        int length = typeArgs.length;
        if (length == 0) {
            throw new IllegalStateException("no type parameters");
        }
        int paramLength = length - 1;
        TypeToken<?>[] paramTypes = new TypeToken<?>[paramLength];
        System.arraycopy(typeArgs, 0, paramTypes, 0, paramLength);
        this.returnType = (TypeToken<R>) typeArgs[paramLength];
        this.paramTypes = unmodifiableList(asList(paramTypes));
    }

    @Override public final TypeToken<R> returnType() {
        return returnType;
    }

    @Override public final List<TypeToken<?>> paramTypes() {
        return paramTypes;
    }
}
