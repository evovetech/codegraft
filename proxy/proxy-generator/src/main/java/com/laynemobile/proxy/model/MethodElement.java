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

package com.laynemobile.proxy.model;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.laynemobile.proxy.Util.Collector;
import com.laynemobile.proxy.Util.Transformer;
import com.laynemobile.proxy.cache.EnvCache;
import com.laynemobile.proxy.cache.MultiAliasCache;
import com.laynemobile.proxy.elements.AliasElements;
import com.laynemobile.proxy.elements.ElementAlias;
import com.laynemobile.proxy.elements.ExecutableElementAlias;
import com.laynemobile.proxy.elements.TypeElementAlias;
import com.laynemobile.proxy.elements.VariableElementAlias;
import com.laynemobile.proxy.types.TypeMirrorAlias;

import java.util.Collection;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import sourcerer.processor.Env;

import static com.laynemobile.proxy.Util.buildList;
import static com.laynemobile.proxy.Util.buildSet;

public final class MethodElement extends AbstractValueAlias<ExecutableElementAlias> {
    private static MultiAliasCache<TypeElementAlias, ExecutableElementAlias, MethodElement> CACHE
            = MultiAliasCache.create(new Creator());

    private final TypeElementAlias typeElement;
    private final TypeMirrorAlias returnType;
    private final ImmutableList<? extends VariableElementAlias> params;
    private final ImmutableList<TypeMirrorAlias> paramTypes;

    private MethodElement(TypeElementAlias typeElement, ExecutableElementAlias element, final Env env) {
        super(element);
        this.typeElement = typeElement;
        this.returnType = element.getReturnType();
        this.params = ImmutableList.copyOf(element.getParameters());
        this.paramTypes
                = buildList(element.getParameters(), new Transformer<TypeMirrorAlias, VariableElementAlias>() {
            @Override public TypeMirrorAlias transform(VariableElementAlias param) {
                env.log("param: %s", param);
                ElementKind paramKind = param.getKind();
                env.log("param kind: %s", paramKind);
                TypeMirror paramType = param.asType();
                env.log("param type: %s", paramType);
                return param.asType();
            }
        });
    }

    public static MultiAliasCache<TypeElementAlias, ExecutableElementAlias, ? extends MethodElement> cache() {
        return CACHE;
    }

    public static ImmutableSet<MethodElement> inherited(final TypeElementAlias typeElement,
            final Collection<? extends TypeElementAlias> superElements, final Env env) {
        return buildSet(superElements, new Collector<MethodElement, TypeElementAlias>() {
            @Override
            public void collect(TypeElementAlias superElement, ImmutableCollection.Builder<MethodElement> out) {
                out.addAll(inherited(typeElement, superElement, env));
            }
        });
    }

    public static ImmutableSet<MethodElement> inherited(final TypeElementAlias typeElement,
            final TypeElementAlias superElement, final Env env) {
        TypeMirror typeMirror = typeElement.asType().actual();
        if (typeMirror.getKind() != TypeKind.DECLARED) {
            return ImmutableSet.of();
        }
        final DeclaredType containing = (DeclaredType) typeMirror;
        final Types types = env.types();
//        if (!types.isAssignable(containing, superElement.asType().actual())) {
//            return ImmutableSet.of();
//        }
        final EnvCache<ExecutableElementAlias, MethodElement> cache = CACHE.getOrCreate(typeElement, env);
        return buildSet(parse(superElement, env), new Transformer<MethodElement, MethodElement>() {
            @Override public MethodElement transform(MethodElement methodElement) {
                ExecutableElement superMethod = methodElement.element().actual();
                env.log("%s -- superMethod: %s", typeElement, superMethod);
                ExecutableType methodType = (ExecutableType) types.asMemberOf(containing, superMethod);
                env.log("%s -- methodType: %s", typeElement, methodType);
                ExecutableElement method = (ExecutableElement) types.asElement(methodType);
                env.log("%s -- method: %s", typeElement, method);
                if (method != null) {
                    return cache.getOrCreate(AliasElements.get(method), env);
                }
                return cache.getOrCreate(AliasElements.get(superMethod), env);
            }
        });
    }

    public static ImmutableList<MethodElement> parse(TypeElementAlias typeElement, final Env env) {
        final EnvCache<ExecutableElementAlias, MethodElement> cache = CACHE.getOrCreate(typeElement, env);
        return buildList(typeElement.getEnclosedElements(), new Transformer<MethodElement, ElementAlias>() {
            @Override public MethodElement transform(ElementAlias element) {
                if (element.getKind() != ElementKind.METHOD) {
                    return null;
                }
                ExecutableElementAlias methodElement = (ExecutableElementAlias) element;
                env.log(methodElement, "processing method element: %s", methodElement);
                return cache.getOrCreate(methodElement, env);
            }
        });
    }

    public boolean overrides(MethodElement overridden, Env env) {
        return env.elements()
                .overrides(element().actual(), overridden.element().actual(), typeElement().actual());
    }

    public final TypeElementAlias typeElement() {
        return typeElement;
    }

    public final ExecutableElementAlias element() {
        return value();
    }

    public TypeMirrorAlias returnType() {
        return returnType;
    }

    public ImmutableList<? extends VariableElementAlias> params() {
        return params;
    }

    public ImmutableList<TypeMirrorAlias> paramTypes() {
        return paramTypes;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MethodElement)) return false;
        return super.equals(o);
    }

    @Override public int hashCode() {
        return super.hashCode();
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("element", element())
                .toString();
    }

    @Override public String toDebugString() {
        return MoreObjects.toStringHelper(this)
                .add("element", element())
                .add("\nreturnType", returnType)
                .add("\nparams", params)
                .add("\nparamTypes", paramTypes)
                .toString();
    }

    private static final class Creator
            implements MultiAliasCache.ValueCreator<TypeElementAlias, ExecutableElementAlias, MethodElement> {

        @Override public MethodElement create(TypeElementAlias typeElement, ExecutableElementAlias element, Env env) {
            MethodElement methodElement = new MethodElement(typeElement, element, env);
            env.log("created method element: %s\n\n", methodElement.toDebugString());
            return methodElement;
        }
    }
}
