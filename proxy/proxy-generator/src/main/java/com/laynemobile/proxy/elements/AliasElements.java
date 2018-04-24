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
import com.laynemobile.proxy.Util;
import com.laynemobile.proxy.cache.AbstractCache;
import com.laynemobile.proxy.types.TypeMirrorAlias;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.SimpleElementVisitor7;

public final class AliasElements {
    private static final Cache CACHE = new Cache();

    private AliasElements() {}

    public static ElementAlias get(Element element) {
        return getInternal(element);
    }

    public static NameAlias get(Name name) {
        return DefaultNameAlias.of(name);
    }

    public static PackageElementAlias get(PackageElement element) {
        return getInternal(element);
    }

    public static TypeElementAlias get(TypeElement element) {
        return getInternal(element);
    }

    public static ExecutableElementAlias get(ExecutableElement element) {
        return getInternal(element);
    }

    public static TypeParameterElementAlias get(TypeParameterElement element) {
        return getInternal(element);
    }

    public static VariableElementAlias get(VariableElement element) {
        return getInternal(element);
    }

    public static ImmutableList<? extends ElementAlias> elements(List<? extends Element> elements) {
        return buildList(elements);
    }

    public static AnnotationMirrorAlias get(AnnotationMirror annotationMirror) {
        return DefaultAnnotationMirrorAlias.of(annotationMirror);
    }

    public static ImmutableList<? extends AnnotationMirrorAlias> annotationMirrors(
            List<? extends AnnotationMirror> annotationMirrors) {
        return DefaultAnnotationMirrorAlias.of(annotationMirrors);
    }

    public static ImmutableList<? extends TypeParameterElementAlias> typeParameters(
            List<? extends TypeParameterElement> typeParameters) {
        return buildList(typeParameters);
    }

    public static ImmutableList<? extends VariableElementAlias> parameters(List<? extends VariableElement> parameters) {
        return buildList(parameters);
    }

    @SuppressWarnings("unchecked")
    private static <K extends Element, V extends ElementAlias> V getInternal(K k) {
        if (k instanceof ElementAlias) {
            return (V) k;
        } else if (k == null) {
            return null;
        }
        return (V) CACHE.get(k);
    }

    private static <K extends Element, V extends ElementAlias> ImmutableList<? extends V> buildList(
            List<? extends K> in) {
        return Util.buildList(in, new Util.Transformer<V, K>() {
            @Override public V transform(K k) {
                return getInternal(k);
            }
        });
    }

    private static final class Cache extends AbstractCache<Element, ElementAlias> {
        private Cache() {}

        @Override protected ForwardingAlias createFutureValue(Element element) {
            return new ForwardingAlias();
        }

        @Override protected ElementAlias create(Element element) {
            log("visiting %s - %s", element.getKind(), element.getSimpleName());
            return element.accept(new Visitor7(), null);
        }

        @Override protected void log(String format, Object... args) {
            // do nothing
        }
    }

    private static final class Visitor7 extends SimpleElementVisitor7<ElementAlias, Void> {
        private Visitor7() {}

        private Visitor7(ElementAlias defaultValue) {
            super(defaultValue);
        }

        @Override protected ElementAlias defaultAction(Element e, Void aVoid) {
            if (e instanceof ElementAlias) {
                return (ElementAlias) e;
            }
            return AbstractElementAlias.unknown(e);
        }

        @Override public ElementAlias visitType(TypeElement e, Void aVoid) {
            return DefaultTypeElementAlias.of(e);
        }

        @Override public ElementAlias visitTypeParameter(TypeParameterElement e, Void aVoid) {
            return DefaultTypeParameterElementAlias.of(e);
        }

        @Override public ElementAlias visitVariable(VariableElement e, Void aVoid) {
            return DefaultVariableElementAlias.of(e);
        }

        @Override public ElementAlias visitExecutable(ExecutableElement e, Void aVoid) {
            return DefaultExecutableElementAlias.of(e);
        }

        @Override public ElementAlias visitPackage(PackageElement e, Void aVoid) {
            return DefaultPackageElementAlias.of(e);
        }
    }

    private static final class ForwardingAlias
            implements ElementAlias,
            TypedElementAlias<Element>,
            TypeElement,
            TypeParameterElement,
            ExecutableElement,
            VariableElement,
            PackageElement,
            AbstractCache.FutureValue<TypedElementAlias<?>> {
        private TypedElementAlias<?> delegate;

        private ForwardingAlias() {}

        @Override public void setDelegate(TypedElementAlias<?> delegate) {
            if (this.delegate == null) {
                this.delegate = delegate;
            }
        }

        @Override public Element actual() {
            return ensure().actual();
        }

        @Override public String toDebugString() {
            return ensure().toDebugString();
        }

        // basic element

        @Override public <R, P> R accept(ElementVisitor<R, P> v, P p) {
            return ensure().accept(v, p);
        }

        @Override public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
            return ensure().getAnnotation(annotationType);
        }

        @Override public ElementKind getKind() {
            return ensure().getKind();
        }

        @Override public Set<Modifier> getModifiers() {
            return ensure().getModifiers();
        }

        @Override public List<? extends AnnotationMirrorAlias> getAnnotationMirrors() {
            return ensure().getAnnotationMirrors();
        }

        @Override public List<? extends ElementAlias> getEnclosedElements() {
            return ensure().getEnclosedElements();
        }

        @Override public ElementAlias getEnclosingElement() {
            return ensure().getEnclosingElement();
        }

        @Override public NameAlias getSimpleName() {
            return ensure().getSimpleName();
        }

        @Override public TypeMirrorAlias asType() {
            return ensure().asType();
        }

        // package element

        @Override public boolean isUnnamed() {
            return packageElement().isUnnamed();
        }

        // type element

        @Override public List<? extends TypeMirrorAlias> getInterfaces() {
            return typeElement().getInterfaces();
        }

        @Override public NestingKind getNestingKind() {
            return typeElement().getNestingKind();
        }

        @Override public NameAlias getQualifiedName() {
            return typeElement().getQualifiedName();
        }

        @Override public TypeMirrorAlias getSuperclass() {
            return typeElement().getSuperclass();
        }

        @Override public List<? extends TypeParameterElementAlias> getTypeParameters() {
            return typeElement().getTypeParameters();
        }

        // type parameter element

        @Override public List<? extends TypeMirrorAlias> getBounds() {
            return typeParameterElement().getBounds();
        }

        @Override public ElementAlias getGenericElement() {
            return typeParameterElement().getGenericElement();
        }

        // executable element

        @Override public List<? extends TypeMirrorAlias> getThrownTypes() {
            return executableElement().getThrownTypes();
        }

        @Override public AnnotationValueAlias getDefaultValue() {
            return executableElement().getDefaultValue();
        }

        @Override public List<? extends VariableElementAlias> getParameters() {
            return executableElement().getParameters();
        }

        @Override public TypeMirrorAlias getReturnType() {
            return executableElement().getReturnType();
        }

        @Override public boolean isVarArgs() {
            return executableElement().isVarArgs();
        }

        // variable element

        @Override public Object getConstantValue() {
            return variableElement().getConstantValue();
        }

        // equals & hash

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ElementAlias)) return false;
            ElementAlias od = o instanceof ForwardingAlias
                    ? ((ForwardingAlias) o).delegate
                    : (ElementAlias) o;
            return Objects.equal(delegate, od);
        }

        @Override public int hashCode() {
            return Objects.hashCode(delegate);
        }

        @Override public String toString() {
            return ensure().toString();
        }

        private TypedElementAlias<?> ensure() {
            TypedElementAlias<?> d = delegate;
            if (d == null) {
                throw new NullPointerException("delegate is null");
            }
            return d;
        }

        @SuppressWarnings("unchecked")
        private <T extends ElementAlias> T cast(String message) {
            try {
                return (T) ensure();
            } catch (ClassCastException e) {
                throw new UnsupportedOperationException(message, e);
            }
        }

        private PackageElementAlias packageElement() {
            return cast("not a PackageElementAlias");
        }

        private TypeElementAlias typeElement() {
            return cast("not a TypeElementAlias");
        }

        private TypeParameterElementAlias typeParameterElement() {
            return cast("not a TypeParameterElementAlias");
        }

        private ExecutableElementAlias executableElement() {
            return cast("not an ExecutableElementAlias");
        }

        private VariableElementAlias variableElement() {
            return cast("not a VariableElementAlias");
        }
    }
}
