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

import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.VariableElement;

final class DefaultVariableElementAlias extends AbstractElementAlias<VariableElement> implements VariableElementAlias {
    private final Object constantValue;

    private DefaultVariableElementAlias(VariableElement element) {
        super(element);
        this.constantValue = element.getConstantValue();
    }

    static VariableElementAlias of(VariableElement element) {
        if (element instanceof VariableElementAlias) {
            return (VariableElementAlias) element;
        }
        return new DefaultVariableElementAlias(element);
    }

    @Override public Object getConstantValue() {
        return constantValue;
    }

    @Override public <R, P> R accept(ElementVisitor<R, P> v, P p) {
        return v.visitVariable(actual(), p);
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultVariableElementAlias)) return false;
        if (!super.equals(o)) return false;
        DefaultVariableElementAlias that = (DefaultVariableElementAlias) o;
        return Objects.equal(constantValue, that.constantValue);
    }

    @Override public int hashCode() {
        return Objects.hashCode(super.hashCode(), constantValue);
    }
}
