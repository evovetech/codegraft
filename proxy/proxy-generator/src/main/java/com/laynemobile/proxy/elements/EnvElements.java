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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.io.Writer;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import sourcerer.processor.Env;

public final class EnvElements implements Elements {
    private final Env env;
    private final Elements elementUtils;

    private EnvElements(Env env) {
        this.env = env;
        this.elementUtils = env.elements();
    }

    public static EnvElements with(Env env) {
        return new EnvElements(env);
    }

    @Override public ImmutableList<? extends AnnotationMirrorAlias> getAllAnnotationMirrors(Element e) {
        return AliasElements.annotationMirrors(elementUtils.getAllAnnotationMirrors(e));
    }

    @Override public ImmutableList<? extends ElementAlias> getAllMembers(TypeElement type) {
        return AliasElements.elements(elementUtils.getAllMembers(type));
    }

    @Override public NameAlias getBinaryName(TypeElement type) {
        return AliasElements.get(elementUtils.getBinaryName(type));
    }

    @Override public String getConstantExpression(Object value) {
        return elementUtils.getConstantExpression(value);
    }

    @Override public String getDocComment(Element e) {
        return elementUtils.getDocComment(e);
    }

    @Override
    public ImmutableMap<? extends ExecutableElementAlias, ? extends AnnotationValueAlias> getElementValuesWithDefaults(
            AnnotationMirror a) {
        return DefaultAnnotationMirrorAlias.map(elementUtils.getElementValuesWithDefaults(a));
    }

    @Override public NameAlias getName(CharSequence cs) {
        return AliasElements.get(elementUtils.getName(cs));
    }

    @Override public PackageElementAlias getPackageElement(CharSequence name) {
        return AliasElements.get(elementUtils.getPackageElement(name));
    }

    @Override public PackageElementAlias getPackageOf(Element type) {
        return AliasElements.get(elementUtils.getPackageOf(type));
    }

    @Override public TypeElementAlias getTypeElement(CharSequence name) {
        return AliasElements.get(elementUtils.getTypeElement(name));
    }

    @Override public boolean hides(Element hider, Element hidden) {
        return elementUtils.hides(hider, hidden);
    }

    @Override public boolean isDeprecated(Element e) {
        return elementUtils.isDeprecated(e);
    }

    @Override public boolean overrides(ExecutableElement overrider,
            ExecutableElement overridden, TypeElement type) {
        return elementUtils.overrides(overrider, overridden, type);
    }

    @Override public void printElements(Writer w, Element... elements) {
        elementUtils.printElements(w, elements);
    }
}
