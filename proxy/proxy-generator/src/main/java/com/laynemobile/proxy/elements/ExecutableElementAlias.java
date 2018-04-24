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

import com.laynemobile.proxy.types.TypeMirrorAlias;

import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeParameterElement;

public interface ExecutableElementAlias extends TypedElementAlias<ExecutableElement>, ExecutableElement {
    /** {@inheritDoc} */
    @Override AnnotationValueAlias getDefaultValue();

    /** {@inheritDoc} */
    @Override List<? extends VariableElementAlias> getParameters();

    /** {@inheritDoc} */
    @Override TypeMirrorAlias getReturnType();

    /** {@inheritDoc} */
    @Override List<? extends TypeMirrorAlias> getThrownTypes();

    /** {@inheritDoc} */
    @Override List<? extends TypeParameterElement> getTypeParameters();

    /** {@inheritDoc} */
    @Override boolean isVarArgs();
}
