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

import com.google.common.collect.ImmutableList;
import com.laynemobile.proxy.elements.AliasElements;
import com.laynemobile.proxy.elements.AnnotationMirrorAlias;

import javax.lang.model.AnnotatedConstruct;
import java.lang.annotation.Annotation;
import java.util.List;

public abstract
class AbstractAnnotatedConstructAlias<T extends AnnotatedConstruct>
        implements TypedAnnotatedConstructAlias<T>
{
    private final T actual;
    private final ImmutableList<? extends AnnotationMirrorAlias> annotationMirrors;

    public
    AbstractAnnotatedConstructAlias(T actual) {
        this.actual = actual;
        this.annotationMirrors = AliasElements.annotationMirrors(actual.getAnnotationMirrors());
    }

    @Override public final
    T actual() {
        return actual;
    }

    @Override public final
    List<? extends AnnotationMirrorAlias> getAnnotationMirrors() {
        return annotationMirrors;
    }

    @Override public
    <A extends Annotation> A getAnnotation(Class<A> aClass) {
        return actual.getAnnotation(aClass);
    }

    @Override public
    <A extends Annotation> A[] getAnnotationsByType(Class<A> aClass) {
        return actual.getAnnotationsByType(aClass);
    }
}
