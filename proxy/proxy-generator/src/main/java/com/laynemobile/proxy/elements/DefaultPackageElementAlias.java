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
import com.laynemobile.proxy.types.AliasTypes;
import com.laynemobile.proxy.types.TypeMirrorAlias;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;

final class DefaultPackageElementAlias implements PackageElementAlias {
    private final PackageElement element;
    private final boolean unnamed;
    private final NameAlias qualifiedName;
    private final NameAlias simpleName;

    private DefaultPackageElementAlias(PackageElement element) {
        this.element = element;
        this.unnamed = element.isUnnamed();
        this.qualifiedName = DefaultNameAlias.of(element.getQualifiedName());
        this.simpleName = DefaultNameAlias.of(element.getSimpleName());
    }

    static PackageElementAlias of(PackageElement element) {
        if (element instanceof PackageElementAlias) {
            return (PackageElementAlias) element;
        }
        return new DefaultPackageElementAlias(element);
    }

    @Override public PackageElement actual() {
        return element;
    }

    @Override public String toDebugString() {
        return toString();
    }

    @Override public boolean isUnnamed() {
        return unnamed;
    }

    @Override public NameAlias getQualifiedName() {
        return qualifiedName;
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

    @Override public <R, P> R accept(ElementVisitor<R, P> v, P p) {
        return v.visitPackage(actual(), p);
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultPackageElementAlias)) return false;
        DefaultPackageElementAlias that = (DefaultPackageElementAlias) o;
        return unnamed == that.unnamed &&
                Objects.equal(qualifiedName, that.qualifiedName);
    }

    @Override public int hashCode() {
        return Objects.hashCode(super.hashCode(), unnamed, qualifiedName);
    }
}
