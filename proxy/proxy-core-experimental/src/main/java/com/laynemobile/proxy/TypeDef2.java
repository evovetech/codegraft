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

package com.laynemobile.proxy;

import com.laynemobile.proxy.functions.ProxyFunctionDef;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public interface TypeDef2<T> extends BaseTypeDef2<T, TypeDef2<? super T>, ProxyFunctionDef<? super T, ?, ?>> {

    class Builder<T> implements com.laynemobile.proxy.Builder<TypeDef2<T>> {
        private final TypeToken<T> type;
        private final Set<TypeDef2<? super T>> superTypes = new HashSet<>();
        private final LinkedHashSet<ProxyFunctionDef<T, ?, ?>> functions = new LinkedHashSet<>();

        protected Builder() {
            this.type = TypeToken.getTypeParameter(getClass());
        }

        public Builder(TypeToken<T> type) {
            this.type = type;
        }

        public Builder<T> addSuperType(TypeDef2<? super T> superType) {
            superTypes.add(superType);
            return this;
        }

        public Builder<T> addSuperType(ProxyType2<? super T> superType) {
            return addSuperType(superType.definition());
        }

        public Builder<T> addFunction(ProxyFunctionDef<T, ?, ?> function) {
            functions.add(function);
            return this;
        }

        @Override public TypeDef2<T> build() {
            return new ConcreteTypeDef2<>(type, superTypes, functions);
        }
    }
}
