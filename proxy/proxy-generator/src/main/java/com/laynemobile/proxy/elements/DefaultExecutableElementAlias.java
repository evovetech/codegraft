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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeParameterElement;

final class DefaultExecutableElementAlias extends AbstractElementAlias<ExecutableElement> implements ExecutableElementAlias {
    private final AnnotationValueAlias defaultValue;
    private final ImmutableList<? extends TypeParameterElementAlias> typeParameters;
    private final TypeMirrorAlias returnType;
    private final ImmutableList<? extends VariableElementAlias> parameters;
    private final ImmutableList<? extends TypeMirrorAlias> thrownTypes;
    private final boolean varArgs;

    private DefaultExecutableElementAlias(ExecutableElement element) {
        super(element);
        this.defaultValue = DefaultAnnotationValueAlias.of(element.getDefaultValue());
        this.typeParameters = AliasElements.typeParameters(element.getTypeParameters());
        this.returnType = AliasTypes.get(element.getReturnType());
        this.parameters = AliasElements.parameters(element.getParameters());
        this.thrownTypes = AliasTypes.list(element.getThrownTypes());
        this.varArgs = element.isVarArgs();
    }

    static ExecutableElementAlias of(ExecutableElement element) {
        if (element instanceof ExecutableElementAlias) {
            return (ExecutableElementAlias) element;
        }
        return new DefaultExecutableElementAlias(element);
    }

    @Override public AnnotationValueAlias getDefaultValue() {
        return defaultValue;
    }

    @Override public ImmutableList<? extends VariableElementAlias> getParameters() {
        return parameters;
    }

    @Override public TypeMirrorAlias getReturnType() {
        return returnType;
    }

    @Override public ImmutableList<? extends TypeMirrorAlias> getThrownTypes() {
        return thrownTypes;
    }

    @Override public ImmutableList<? extends TypeParameterElement> getTypeParameters() {
        return typeParameters;
    }

    @Override public boolean isVarArgs() {
        return varArgs;
    }

    @Override public <R, P> R accept(ElementVisitor<R, P> v, P p) {
        return v.visitExecutable(actual(), p);
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultExecutableElementAlias)) return false;
        if (!super.equals(o)) return false;
        DefaultExecutableElementAlias that = (DefaultExecutableElementAlias) o;
        return Objects.equal(defaultValue, that.defaultValue) &&
                Objects.equal(typeParameters, that.typeParameters) &&
                Objects.equal(returnType, that.returnType) &&
                Objects.equal(parameters, that.parameters) &&
                Objects.equal(thrownTypes, that.thrownTypes);
    }

    @Override public int hashCode() {
        return Objects.hashCode(super.hashCode(), defaultValue, typeParameters, returnType, parameters,
                thrownTypes);
    }
}
