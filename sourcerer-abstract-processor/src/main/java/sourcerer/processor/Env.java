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

package sourcerer.processor;

import java.util.Collections;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.NOTE;

public class Env {
    public static final String ALL_ANNOTATIONS = "*";
    public static final Set<String> ALL_ANNOTATION_TYPES = Collections.singleton(ALL_ANNOTATIONS);

    private final ProcessingEnvironment processingEnv;
    private final Messager messager;
    private final Elements elements;
    private final Types types;
    private final Filer filer;

    public Env(Env env) {
        this(env.processingEnv);
    }

    public Env(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
        this.messager = processingEnv.getMessager();
        this.elements = processingEnv.getElementUtils();
        this.types = processingEnv.getTypeUtils();
        this.filer = processingEnv.getFiler();
    }

    public ProcessingEnvironment processingEnv() {
        return processingEnv;
    }

    public Messager messager() {
        return messager;
    }

    public Elements elements() {
        return elements;
    }

    public Types types() {
        return types;
    }

    public Filer filer() {
        return filer;
    }

    public final void log(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        messager().printMessage(NOTE, message, element);
    }

    public final void log(String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        messager().printMessage(NOTE, message);
    }

    public final void error(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        messager().printMessage(ERROR, message, element);
    }

    public final void error(String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        messager().printMessage(ERROR, message);
    }
}
