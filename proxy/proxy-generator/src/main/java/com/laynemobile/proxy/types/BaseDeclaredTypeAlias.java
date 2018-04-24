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

import com.laynemobile.proxy.elements.ElementAlias;

import java.util.List;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVisitor;

public interface BaseDeclaredTypeAlias<T extends DeclaredType> extends TypedTypeMirrorAlias<T>, DeclaredType {
    /** {@inheritDoc} */
    @Override TypeKind getKind();

    /** {@inheritDoc} */
    @Override ElementAlias asElement();

    /** {@inheritDoc} */
    @Override TypeMirrorAlias getEnclosingType();

    /** {@inheritDoc} */
    @Override List<? extends TypeMirrorAlias> getTypeArguments();

    /** {@inheritDoc} */
    @Override <R, P> R accept(TypeVisitor<R, P> v, P p);
}
