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

package com.laynemobile.proxy.types;

import com.laynemobile.proxy.elements.AliasElements;
import com.laynemobile.proxy.elements.ElementAlias;

import java.util.List;

import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeVisitor;

final class DefaultErrorTypeAlias extends AbstractTypeMirrorAlias<ErrorType> implements ErrorTypeAlias {
    private DefaultErrorTypeAlias(ErrorType declaredType) {
        super(declaredType);
    }

    static ErrorTypeAlias of(ErrorType declaredType) {
        if (declaredType instanceof ErrorTypeAlias) {
            return (ErrorTypeAlias) declaredType;
        }
        return new DefaultErrorTypeAlias(declaredType);
    }

    @Override public ElementAlias asElement() {
        return AliasElements.get(actual().asElement());
    }

    @Override public TypeMirrorAlias getEnclosingType() {
        return AliasTypes.get(actual().getEnclosingType());
    }

    @Override public List<? extends TypeMirrorAlias> getTypeArguments() {
        return AliasTypes.list(actual().getTypeArguments());
    }

    @Override public <R, P> R accept(TypeVisitor<R, P> v, P p) {
        return v.visitError(actual(), p);
    }

    @Override public boolean equals(Object o) {
        return o instanceof DefaultErrorTypeAlias && super.equals(o);
    }
}
