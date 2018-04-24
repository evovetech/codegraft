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

package com.laynemobile.proxy;

import com.google.common.collect.ImmutableSet;
import com.laynemobile.proxy.annotations.GenerateProxyHandler;
import com.laynemobile.proxy.annotations.Generated;
import com.laynemobile.proxy.internal.ProxyLog;

import java.io.IOException;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;

import sourcerer.processor.Env;
import sourcerer.processor.Template;

public class ProxyTemplate extends Template {
    private ProxyRound round;

    public ProxyTemplate(Env env) {
        super(env);
        this.round = ProxyRound.begin(env);
    }

    @Override public Set<String> supportedAnnotationTypes() {
        return ImmutableSet.<String>builder()
                .add(GenerateProxyHandler.class.getCanonicalName())
                .add(Generated.class.getCanonicalName())
                .build();
    }

    @Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            round = round.process(annotations, roundEnv);
            log("round=%s", round);
            log("  ");
        } catch (IOException e) {
            error("error writing: %s", ProxyLog.getStackTraceString(e));
            return true; // exit processing
        }

//        for (Element element : roundEnv.getElementsAnnotatedWith(Generated.class)) {
//            // Ensure it is a class element
//            if (element.getKind() != ElementKind.CLASS) {
//                error(element, "Only classes can be annotated with @%s",
//                        Generated.class.getSimpleName());
//                return true; // Exit processing
//            }
//
//            log("processing generated type! %s", element);
//
//            final Generated annotation = element.getAnnotation(Generated.class);
//            if (annotation != null) {
//                log("has annotation: %s\n\n", annotation);
//            }
//        }
//
//        for (Element element : roundEnv.getElementsAnnotatedWith(ProxyFunctionImplementation.class)) {
//            // Ensure it is a class element
//            if (element.getKind() != ElementKind.CLASS) {
//                error(element, "Only classes can be annotated with @%s",
//                        ProxyFunctionImplementation.class.getSimpleName());
//                return true; // Exit processing
//            }
//
//            log("say\n\n");
//            log(element, "processing abstract function type!\n\n");
//
//            final ProxyFunctionImplementation annotation = element.getAnnotation(ProxyFunctionImplementation.class);
//            if (annotation != null) {
//                log(element, "has annotation: %s", annotation);
//                TypeElement implementation = Util.parse(new Func0<Class<?>>() {
//                    @Override public Class<?> call() {
//                        return annotation.value();
//                    }
//                }, this);
//                log(element, "implementation = %s", implementation);
//            }
//        }

        return false;
    }
}
