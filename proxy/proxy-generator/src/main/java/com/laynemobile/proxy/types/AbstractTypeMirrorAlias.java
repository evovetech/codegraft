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

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

abstract class AbstractTypeMirrorAlias<T extends TypeMirror> implements TypedTypeMirrorAlias<T> {
    private final T typeMirror;
    private final TypeKind kind;
    private final String toString;

    AbstractTypeMirrorAlias(T typeMirror) {
        this.typeMirror = typeMirror;
        this.kind = typeMirror.getKind();
        this.toString = typeMirror.toString();
    }

    static TypeMirrorAlias unknown(TypeMirror typeMirror) {
        return new AbstractTypeMirrorAlias<TypeMirror>(typeMirror) {
            @Override public <R, P> R accept(TypeVisitor<R, P> v, P p) {
                return v.visitUnknown(this, p);
            }
        };
    }

    @Override public T actual() {
        return typeMirror;
    }

    @Override public String toDebugString() {
        return toString();
    }

    @Override public final TypeKind getKind() {
        return kind;
    }

    @Override public final String toString() {
        return toString;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractTypeMirrorAlias)) return false;
        AbstractTypeMirrorAlias that = (AbstractTypeMirrorAlias) o;
        return kind == that.kind &&
                Objects.equal(toString, that.toString);
    }

    @Override public int hashCode() {
        return Objects.hashCode(kind, toString);
    }
}
