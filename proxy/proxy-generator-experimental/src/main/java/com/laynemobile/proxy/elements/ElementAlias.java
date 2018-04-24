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

import com.laynemobile.proxy.AnnotatedConstructAlias;
import com.laynemobile.proxy.types.TypeMirrorAlias;

import javax.lang.model.element.Element;
import java.util.List;

public
interface ElementAlias
        extends Element,
        AnnotatedConstructAlias
{
    @Override
    Element actual();

    /**
     * {@inheritDoc}
     */
    @Override
    TypeMirrorAlias asType();

    /**
     * {@inheritDoc}
     */
    @Override
    NameAlias getSimpleName();

    /**
     * {@inheritDoc}
     */
    @Override
    ElementAlias getEnclosingElement();

    /**
     * {@inheritDoc}
     */
    @Override
    List<? extends ElementAlias> getEnclosedElements();
}
