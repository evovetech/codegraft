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

package com.laynemobile.proxy.elements;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.laynemobile.proxy.types.AliasTypes;
import com.laynemobile.proxy.types.TypeMirrorAlias;

import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.TypeParameterElement;

final class DefaultTypeParameterElementAlias extends AbstractElementAlias<TypeParameterElement> implements TypeParameterElementAlias {
    private final ElementAlias genericElement;
    private final ImmutableList<? extends TypeMirrorAlias> bounds;

    private DefaultTypeParameterElementAlias(TypeParameterElement element) {
        super(element);
        this.genericElement = AliasElements.get(element.getGenericElement());
        this.bounds = AliasTypes.list(element.getBounds());
    }

    static TypeParameterElementAlias of(TypeParameterElement element) {
        if (element instanceof TypeParameterElementAlias) {
            return (TypeParameterElementAlias) element;
        }
        return new DefaultTypeParameterElementAlias(element);
    }

    @Override public ElementAlias getGenericElement() {
        return genericElement;
    }

    @Override public ImmutableList<? extends TypeMirrorAlias> getBounds() {
        return bounds;
    }

    @Override public <R, P> R accept(ElementVisitor<R, P> v, P p) {
        return v.visitTypeParameter(actual(), p);
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultTypeParameterElementAlias)) return false;
        if (!super.equals(o)) return false;
        DefaultTypeParameterElementAlias that = (DefaultTypeParameterElementAlias) o;
        return Objects.equal(genericElement, that.genericElement) &&
                Objects.equal(bounds, that.bounds);
    }

    @Override public int hashCode() {
        return Objects.hashCode(super.hashCode(), genericElement, bounds);
    }
}
