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

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.laynemobile.proxy.types.AliasTypes;
import com.laynemobile.proxy.types.TypeMirrorAlias;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;

abstract class AbstractElementAlias<E extends Element> implements TypedElementAlias<E> {
    private final E element;
    private final ElementKind kind;
    private final NameAlias simpleName;
    private final TypeMirrorAlias type;
    private final ElementAlias enclosingElement;
    private final ImmutableList<? extends AnnotationMirrorAlias> annotationMirrors;
    private final ImmutableList<? extends ElementAlias> enclosedElements;
    private final ImmutableSet<Modifier> modifiers;
    private final String toString;

    AbstractElementAlias(E element) {
        this.kind = element.getKind();
        this.simpleName = DefaultNameAlias.of(element.getSimpleName());
        this.type = AliasTypes.get(element.asType());
        this.enclosingElement = AliasElements.get(element);
        this.annotationMirrors = DefaultAnnotationMirrorAlias.of(element.getAnnotationMirrors());
        this.enclosedElements = AliasElements.elements(element.getEnclosedElements());
        this.modifiers = ImmutableSet.copyOf(element.getModifiers());
        this.toString = element.toString();
        this.element = element;
    }

    static ElementAlias unknown(Element element) {
        final class UnknownElementAlias extends AbstractElementAlias<Element> {
            private UnknownElementAlias(Element element) {
                super(element);
            }

            @Override public <R, P> R accept(ElementVisitor<R, P> v, P p) {
                return v.visitUnknown(this, p);
            }
        }
        return new UnknownElementAlias(element);
    }

    @Override public final E actual() {
        return element;
    }

    @Override public TypeMirrorAlias asType() {
        return type;
    }

    @Override public NameAlias getSimpleName() {
        return simpleName;
    }

    @Override public ImmutableList<? extends AnnotationMirrorAlias> getAnnotationMirrors() {
        return annotationMirrors;
    }

    @Override public ElementAlias getEnclosingElement() {
        return enclosingElement;
    }

    @Override public ImmutableList<? extends ElementAlias> getEnclosedElements() {
        return enclosedElements;
    }

    @Override public ElementKind getKind() {
        return kind;
    }

    @Override public Set<Modifier> getModifiers() {
        return modifiers;
    }

    @Override public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        // TODO:!!!
        return element.getAnnotation(annotationType);
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractElementAlias)) return false;
        AbstractElementAlias that = (AbstractElementAlias) o;
        return kind == that.kind &&
                Objects.equal(simpleName, that.simpleName) &&
                Objects.equal(annotationMirrors, that.annotationMirrors) &&
                Objects.equal(enclosedElements, that.enclosedElements) &&
                Objects.equal(modifiers, that.modifiers);
    }

    @Override public int hashCode() {
        return Objects.hashCode(kind, simpleName, annotationMirrors, enclosedElements, modifiers);
    }

    @Override public final String toString() {
        return toString;
    }

    @Override public String toDebugString() {
        return MoreObjects.toStringHelper(this)
                .add("annotationMirrors", annotationMirrors)
                .add("element", element)
                .add("kind", kind)
                .add("simpleName", simpleName)
                .add("type", type)
                .add("enclosingElement", enclosingElement)
                .add("enclosedElements", enclosedElements)
                .add("modifiers", modifiers)
                .add("toString", toString)
                .toString();
    }
}
