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

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.laynemobile.proxy.Util;
import com.laynemobile.proxy.types.AliasTypes;
import com.laynemobile.proxy.types.DeclaredTypeAlias;

import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;

final class DefaultAnnotationMirrorAlias implements AnnotationMirrorAlias {
    private final DeclaredTypeAlias annotationType;
    private final ImmutableMap<? extends ExecutableElementAlias, ? extends AnnotationValueAlias> elementValues;

    private DefaultAnnotationMirrorAlias(AnnotationMirror annotationMirror) {
        this.annotationType = AliasTypes.get(annotationMirror.getAnnotationType());
        this.elementValues = map(annotationMirror.getElementValues());
    }

    static AnnotationMirrorAlias of(AnnotationMirror annotationMirror) {
        if (annotationMirror instanceof AnnotationMirrorAlias) {
            return (AnnotationMirrorAlias) annotationMirror;
        }
        return new DefaultAnnotationMirrorAlias(annotationMirror);
    }

    static ImmutableList<? extends AnnotationMirrorAlias> of(List<? extends AnnotationMirror> annotationMirrors) {
        return Util.buildList(annotationMirrors, new Util.Transformer<AnnotationMirrorAlias, AnnotationMirror>() {
            @Override public AnnotationMirrorAlias transform(AnnotationMirror annotationMirror) {
                return of(annotationMirror);
            }
        });
    }

    static ImmutableMap<? extends ExecutableElementAlias, ? extends AnnotationValueAlias> map(
            Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues) {
        return Util.buildMap(elementValues, new KeyTransformer(), new ValueTransformer());
    }

    @Override public DeclaredTypeAlias getAnnotationType() {
        return annotationType;
    }

    @Override public ImmutableMap<? extends ExecutableElement, ? extends AnnotationValueAlias> getElementValues() {
        return elementValues;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultAnnotationMirrorAlias)) return false;
        DefaultAnnotationMirrorAlias that = (DefaultAnnotationMirrorAlias) o;
        return Objects.equal(annotationType, that.annotationType) &&
                Objects.equal(elementValues, that.elementValues);
    }

    @Override public int hashCode() {
        return Objects.hashCode(annotationType, elementValues);
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("annotationType", annotationType)
                .add("elementValues", elementValues)
                .toString();
    }

    private static final class KeyTransformer implements Util.Transformer<ExecutableElementAlias, ExecutableElement> {
        @Override public ExecutableElementAlias transform(ExecutableElement element) {
            return AliasElements.get(element);
        }
    }

    private static final class ValueTransformer implements Util.Transformer<AnnotationValueAlias, AnnotationValue> {
        @Override public AnnotationValueAlias transform(AnnotationValue annotationValue) {
            return DefaultAnnotationValueAlias.of(annotationValue);
        }
    }
}
