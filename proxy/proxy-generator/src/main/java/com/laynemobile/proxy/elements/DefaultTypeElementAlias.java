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

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;

import static com.laynemobile.proxy.Util.runtime;

final class DefaultTypeElementAlias implements TypeElementAlias {
    private final TypeElement element;
    private final NameAlias qualifiedName;
    private final NameAlias simpleName;
    private final NestingKind nestingKind;
    private final TypeMirrorAlias superClass;
    private final ImmutableList<? extends TypeMirrorAlias> interfaces;
    private final ImmutableList<? extends TypeParameterElementAlias> typeParameters;
    private final String toString;

    private DefaultTypeElementAlias(TypeElement element) {
        try {
            this.element = element;
            this.qualifiedName = DefaultNameAlias.of(element.getQualifiedName());
            this.simpleName = DefaultNameAlias.of(element.getSimpleName());
            this.nestingKind = element.getNestingKind();
            this.superClass = AliasTypes.get(element.getSuperclass());
            this.interfaces = AliasTypes.list(element.getInterfaces());
            this.typeParameters = AliasElements.typeParameters(element.getTypeParameters());
            this.toString = element.toString();
        } catch (Exception e) {
            throw runtime(element, e);
        }
    }

    static TypeElementAlias of(TypeElement element) {
        if (element instanceof TypeElementAlias) {
            return (TypeElementAlias) element;
        }
        return new DefaultTypeElementAlias(element);
    }

    @Override public TypeElement actual() {
        return element;
    }

    @Override public String toDebugString() {
        return toString();
    }

    @Override public TypeMirrorAlias asType() {
        return AliasTypes.get(element.asType());
    }

    @Override public NameAlias getSimpleName() {
        return simpleName;
    }

    @Override public List<? extends AnnotationMirrorAlias> getAnnotationMirrors() {
        return DefaultAnnotationMirrorAlias.of(element.getAnnotationMirrors());
    }

    @Override public ElementAlias getEnclosingElement() {
        return AliasElements.get(element.getEnclosingElement());
    }

    @Override public List<? extends ElementAlias> getEnclosedElements() {
        return AliasElements.elements(element.getEnclosedElements());
    }

    @Override public ElementKind getKind() {
        return element.getKind();
    }

    @Override public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return element.getAnnotation(annotationType);
    }

    @Override public Set<Modifier> getModifiers() {
        return element.getModifiers();
    }

    @Override public NameAlias getQualifiedName() {
        return qualifiedName;
    }

    @Override public NestingKind getNestingKind() {
        return nestingKind;
    }

    @Override public TypeMirrorAlias getSuperclass() {
        return superClass;
    }

    @Override public ImmutableList<? extends TypeMirrorAlias> getInterfaces() {
        return interfaces;
    }

    @Override public ImmutableList<? extends TypeParameterElementAlias> getTypeParameters() {
        return typeParameters;
    }

    @Override public <R, P> R accept(ElementVisitor<R, P> v, P p) {
        return v.visitType(actual(), p);
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultTypeElementAlias)) return false;
        DefaultTypeElementAlias that = (DefaultTypeElementAlias) o;
        return Objects.equal(qualifiedName, that.qualifiedName) &&
                nestingKind == that.nestingKind;
    }

    @Override public int hashCode() {
        return Objects.hashCode(super.hashCode(), qualifiedName, nestingKind);
    }

    @Override public String toString() {
        return toString;
    }
}
