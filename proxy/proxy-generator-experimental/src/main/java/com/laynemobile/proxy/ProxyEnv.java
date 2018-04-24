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

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

import sourcerer.processor.Env;

public class ProxyEnv extends Env {
    private static final Messager MESSAGER = new Messager() {
        @Override public void printMessage(Diagnostic.Kind kind, CharSequence msg) {
            System.out.printf("%s: %s\n", kind, msg);
        }

        @Override public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e) {
            System.out.printf("%s: e='%s' - %s\n", kind, e, msg);
        }

        @Override public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e, AnnotationMirror a) {
            System.out.printf("%s: e='%s', a='%s' - %s\n", kind, e, a, msg);
        }

        @Override
        public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e, AnnotationMirror a,
                AnnotationValue v) {
            System.out.printf("%s: e='%s', a='%s', v='%s' - %s\n", kind, e, a, v, msg);
        }
    };

    protected ProxyEnv(Env env) {
        super(env);
    }

    protected ProxyEnv(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    public static ProxyEnv create(ProcessingEnvironment processingEnv) {
        return new ProxyEnv(processingEnv);
    }

    public static ProxyEnv wrap(Env env) {
        if (env instanceof ProxyEnv) {
            return (ProxyEnv) env;
        }
        return new ProxyEnv(env);
    }

    @Override public final Messager messager() {
        return MESSAGER;
    }
}
