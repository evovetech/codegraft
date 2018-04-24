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

import com.google.common.base.Objects;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeVisitor;

final class DefaultArrayTypeAlias extends AbstractTypeMirrorAlias<ArrayType> implements ArrayTypeAlias {
    private final TypeMirrorAlias componentType;

    private DefaultArrayTypeAlias(ArrayType typeMirror) {
        super(typeMirror);
        this.componentType = AliasTypes.get(typeMirror.getComponentType());
    }

    static ArrayTypeAlias of(ArrayType arrayType) {
        if (arrayType instanceof ArrayTypeAlias) {
            return (ArrayTypeAlias) arrayType;
        }
        return new DefaultArrayTypeAlias(arrayType);
    }

    @Override public <R, P> R accept(TypeVisitor<R, P> v, P p) {
        return v.visitArray(actual(), p);
    }

    @Override public TypeMirrorAlias getComponentType() {
        return componentType;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultArrayTypeAlias)) return false;
        if (!super.equals(o)) return false;
        DefaultArrayTypeAlias that = (DefaultArrayTypeAlias) o;
        return Objects.equal(componentType, that.componentType);
    }

    @Override public int hashCode() {
        return Objects.hashCode(super.hashCode(), componentType);
    }
}
