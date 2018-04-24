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
import com.google.common.collect.ImmutableList;
import com.laynemobile.proxy.cache.AliasCache;
import com.laynemobile.proxy.elements.TypeElementAlias;
import com.laynemobile.proxy.types.AliasTypes;
import com.laynemobile.proxy.types.DeclaredTypeAlias;

import java.util.List;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import sourcerer.processor.Env;

public final class ProxyType extends AbstractValueAlias<DeclaredTypeAlias> {
    private final ProxyElement element;
    private final ImmutableList<ProxyType> directSuperTypes;

    private ProxyType(DeclaredTypeAlias type, ProxyElement element, List<ProxyType> directSuperTypes) {
        super(type);
        this.element = element;
        this.directSuperTypes = ImmutableList.copyOf(directSuperTypes);
    }

    public static AliasCache<DeclaredTypeAlias, ? extends ProxyType, TypeMirror> cache() {
        return Cache.INSTANCE;
    }

    public DeclaredTypeAlias type() {
        return value();
    }

    public ProxyElement element() {
        return element;
    }

    public ImmutableList<ProxyType> directSuperTypes() {
        return directSuperTypes;
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("element", element)
                .add("directSuperTypes", directSuperTypes)
                .toString();
    }

    @Override public String toDebugString() {
        return MoreObjects.toStringHelper(this)
                .add("type", type())
                .add("element", element.toDebugString())
                .add("directSuperTypes", directSuperTypes)
                .toString();
    }

    private static final class Cache extends AliasCache<DeclaredTypeAlias, ProxyType, TypeMirror> {
        private static final Cache INSTANCE = new Cache();

        private Cache() {}

        @Override protected DeclaredTypeAlias cast(TypeMirror typeMirror, Env env) throws Exception {
            if (typeMirror.getKind() != TypeKind.DECLARED) {
                return null;
            }
            return AliasTypes.get((DeclaredType) typeMirror);
        }

        @Override protected ProxyType create(DeclaredTypeAlias declaredType, Env env) {
            ProxyElement proxyElement = ProxyElement.cache()
                    .getOrCreate((TypeElementAlias) declaredType.asElement(), env);

            ImmutableList.Builder<ProxyType> directSuperTypes = ImmutableList.builder();
            for (TypeMirror typeMirror : env.types().directSupertypes(declaredType.actual())) {
                if (typeMirror.getKind() == TypeKind.DECLARED) {
                    directSuperTypes.add(getOrCreate(AliasTypes.get((DeclaredType) typeMirror), env));
                }
            }
            return new ProxyType(declaredType, proxyElement, directSuperTypes.build());
        }
    }
}
