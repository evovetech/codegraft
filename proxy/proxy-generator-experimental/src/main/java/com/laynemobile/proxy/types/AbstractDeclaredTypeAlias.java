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
import com.google.common.collect.ImmutableList;
import com.laynemobile.proxy.elements.AliasElements;
import com.laynemobile.proxy.elements.ElementAlias;

import javax.lang.model.type.DeclaredType;

abstract class AbstractDeclaredTypeAlias<T extends DeclaredType> extends AbstractTypeMirrorAlias<T>
        implements BaseDeclaredTypeAlias<T> {
    private final ElementAlias element;
    private final TypeMirrorAlias enclosingType;
    private final ImmutableList<? extends TypeMirrorAlias> typeArguments;

    AbstractDeclaredTypeAlias(T declaredType) {
        super(declaredType);
        this.element = AliasElements.get(declaredType.asElement());
        this.enclosingType = AliasTypes.get(declaredType.getEnclosingType());
        this.typeArguments = AliasTypes.list(declaredType.getTypeArguments());
    }

    @Override public ElementAlias asElement() {
        return element;
    }

    @Override public TypeMirrorAlias getEnclosingType() {
        return enclosingType;
    }

    @Override public ImmutableList<? extends TypeMirrorAlias> getTypeArguments() {
        return typeArguments;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractDeclaredTypeAlias)) return false;
        if (!super.equals(o)) return false;
        AbstractDeclaredTypeAlias that = (AbstractDeclaredTypeAlias) o;
        return Objects.equal(element, that.element) &&
                Objects.equal(typeArguments, that.typeArguments);
    }

    @Override public int hashCode() {
        return Objects.hashCode(super.hashCode(), element, typeArguments);
    }
}
