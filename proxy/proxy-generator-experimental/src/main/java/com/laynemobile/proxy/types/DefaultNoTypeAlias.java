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

import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeVisitor;

final class DefaultNoTypeAlias extends AbstractTypeMirrorAlias<NoType> implements NoTypeAlias {
    private DefaultNoTypeAlias(NoType typeMirror) {
        super(typeMirror);
    }

    static NoTypeAlias of(NoType noType) {
        if (noType instanceof NoTypeAlias) {
            return (NoTypeAlias) noType;
        }
        return new DefaultNoTypeAlias(noType);
    }

    @Override public <R, P> R accept(TypeVisitor<R, P> v, P p) {
        return v.visitNoType(actual(), p);
    }

    @Override public boolean equals(Object o) {
        return o instanceof DefaultNoTypeAlias && super.equals(o);
    }
}
