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

package com.laynemobile.proxy.functions;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;

import java.io.IOException;
import java.util.Set;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

import sourcerer.processor.BaseProcessor;

@AutoService(Processor.class)
public class CodeWriter extends BaseProcessor {
    @Override public Set<String> getSupportedAnnotationTypes() {
        return ImmutableSet.<String>builder()
                .add(GenerateFunctionDefs.class.getCanonicalName())
                .build();
    }

    @Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(GenerateFunctionDefs.class)) {
            if (element.getKind() != ElementKind.PACKAGE) {
                continue;
            }
            PackageElement packageElement = (PackageElement) element;
            String packageName = packageElement.getQualifiedName().toString();
            int i;
            int iterations = 8;
            for (i = 1; i < iterations + 1; i++) {
                write(new ActionDefTemplate(packageName, i).fill());
                write(new FuncDefTemplate(packageName, i).fill());
                write(new ProxyActionDefTemplate(packageName, i).fill());
                write(new ProxyFuncDefTemplate(packageName, i).fill());
            }
            write(new ActionDefTemplate(packageName, i).fill());
            write(new FuncDefTemplate(packageName, i).fill());
        }
        for (Element element : roundEnv.getElementsAnnotatedWith(GenerateFunctionTransforms.class)) {
            if (element.getKind() != ElementKind.PACKAGE) {
                continue;
            }
            PackageElement packageElement = (PackageElement) element;
            String packageName = packageElement.getQualifiedName().toString();
            int i;
            int iterations = 8;
            for (i = 1; i < iterations + 1; i++) {
                write(new ActionTransformTemplate(packageName, i).fill());
                write(new FuncTransformTemplate(packageName, i).fill());
                write(new ProxyActionTransformTemplate(packageName, i).fill());
                write(new ProxyFuncTransformTemplate(packageName, i).fill());
            }
            write(new ActionTransformTemplate(packageName, i).fill());
            write(new FuncTransformTemplate(packageName, i).fill());
        }
        return false;
    }

    private void write(FileTemplate template) {
        try {
            template.writeTo(env().filer());
        } catch (IOException e) {
            error("error processing: %s", e.getMessage());
        }
    }
}
