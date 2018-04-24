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

import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.WildcardType;

final class DefaultWildcardTypeAlias extends AbstractTypeMirrorAlias<WildcardType> implements WildcardTypeAlias {
    private final TypeMirrorAlias superBound;
    private final TypeMirrorAlias extendsBound;

    private DefaultWildcardTypeAlias(WildcardType typeMirror) {
        super(typeMirror);
        this.superBound = AliasTypes.get(typeMirror.getSuperBound());
        this.extendsBound = AliasTypes.get(typeMirror.getExtendsBound());
    }

    static WildcardTypeAlias of(WildcardType wildcardType) {
        if (wildcardType instanceof WildcardTypeAlias) {
            return (WildcardTypeAlias) wildcardType;
        }
        return new DefaultWildcardTypeAlias(wildcardType);
    }

    @Override public TypeMirrorAlias getExtendsBound() {
        return extendsBound;
    }

    @Override public TypeMirrorAlias getSuperBound() {
        return superBound;
    }

    @Override public <R, P> R accept(TypeVisitor<R, P> v, P p) {
        return v.visitWildcard(actual(), p);
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultWildcardTypeAlias)) return false;
        if (!super.equals(o)) return false;
        DefaultWildcardTypeAlias that = (DefaultWildcardTypeAlias) o;
        return Objects.equal(superBound, that.superBound) &&
                Objects.equal(extendsBound, that.extendsBound);
    }

    @Override public int hashCode() {
        return Objects.hashCode(super.hashCode(), superBound, extendsBound);
    }
}
