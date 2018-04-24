/*
 * Copyright 2018 evove.tech
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

package com.laynemobile.proxy;

import com.laynemobile.proxy.elements.AnnotationMirrorAlias;

import javax.lang.model.AnnotatedConstruct;
import java.lang.annotation.Annotation;
import java.util.List;

public
interface TypedAnnotatedConstructAlias<T extends AnnotatedConstruct>
        extends AnnotatedConstructAlias,
        Alias<T>
{
    @Override
    T actual();

    @Override
    List<? extends AnnotationMirrorAlias> getAnnotationMirrors();

    @Override
    <A extends Annotation> A getAnnotation(Class<A> aClass);

    @Override
    <A extends Annotation> A[] getAnnotationsByType(Class<A> aClass);
}
