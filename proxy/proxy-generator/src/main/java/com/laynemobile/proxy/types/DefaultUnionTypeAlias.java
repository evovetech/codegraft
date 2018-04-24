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
import com.google.common.collect.ImmutableList;

import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.UnionType;

final class DefaultUnionTypeAlias extends AbstractTypeMirrorAlias<UnionType> implements UnionTypeAlias {
    private final ImmutableList<? extends TypeMirrorAlias> alternatives;

    private DefaultUnionTypeAlias(UnionType typeMirror) {
        super(typeMirror);
        this.alternatives = AliasTypes.list(typeMirror.getAlternatives());
    }

    static UnionTypeAlias of(UnionType unionType) {
        if (unionType instanceof UnionTypeAlias) {
            return (UnionTypeAlias) unionType;
        }
        return new DefaultUnionTypeAlias(unionType);
    }

    @Override public <R, P> R accept(TypeVisitor<R, P> v, P p) {
        return v.visitUnion(actual(), p);
    }

    @Override public ImmutableList<? extends TypeMirrorAlias> getAlternatives() {
        return alternatives;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultUnionTypeAlias)) return false;
        if (!super.equals(o)) return false;
        DefaultUnionTypeAlias that = (DefaultUnionTypeAlias) o;
        return Objects.equal(alternatives, that.alternatives);
    }

    @Override public int hashCode() {
        return Objects.hashCode(super.hashCode(), alternatives);
    }
}
