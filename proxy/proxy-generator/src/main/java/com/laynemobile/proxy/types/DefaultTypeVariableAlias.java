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
import com.laynemobile.proxy.elements.AliasElements;
import com.laynemobile.proxy.elements.ElementAlias;

import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;

final class DefaultTypeVariableAlias extends AbstractTypeMirrorAlias<TypeVariable> implements TypeVariableAlias {
    private final ElementAlias element;
    private final TypeMirrorAlias lowerBound;
    private final TypeMirrorAlias upperBound;

    private DefaultTypeVariableAlias(TypeVariable typeMirror) {
        super(typeMirror);
        this.element = AliasElements.get(typeMirror.asElement());
        this.lowerBound = AliasTypes.get(typeMirror.getLowerBound());
        this.upperBound = AliasTypes.get(typeMirror.getUpperBound());
    }

    static TypeVariableAlias of(TypeVariable typeVariable) {
        if (typeVariable instanceof TypeVariableAlias) {
            return (TypeVariableAlias) typeVariable;
        }
        return new DefaultTypeVariableAlias(typeVariable);
    }

    @Override public ElementAlias asElement() {
        return element;
    }

    @Override public TypeMirrorAlias getLowerBound() {
        return lowerBound;
    }

    @Override public TypeMirrorAlias getUpperBound() {
        return upperBound;
    }

    @Override public <R, P> R accept(TypeVisitor<R, P> v, P p) {
        return v.visitTypeVariable(actual(), p);
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultTypeVariableAlias)) return false;
        if (!super.equals(o)) return false;
        DefaultTypeVariableAlias that = (DefaultTypeVariableAlias) o;
        return Objects.equal(element, that.element) &&
                Objects.equal(lowerBound, that.lowerBound) &&
                Objects.equal(upperBound, that.upperBound);
    }

    @Override public int hashCode() {
        return Objects.hashCode(super.hashCode(), element, lowerBound, upperBound);
    }
}
