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

package com.laynemobile.proxy.model;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import sourcerer.processor.Env;

public abstract class EnvRound<R extends EnvRound<R>> extends AbstractRound<R> {
    private final ProxyEnv env;

    protected EnvRound(Env env) {
        if (env == null) {
            throw new NullPointerException("env cannot be null");
        }
        this.env = ProxyEnv.wrap(env);
    }

    protected EnvRound(R previous) {
        super(previous);
        this.env = previous.env();
    }

    public final ProxyEnv env() {
        return env;
    }

    public final ProcessingEnvironment processingEnv() {
        return env.processingEnv();
    }

    public final Messager messager() {
        return env.messager();
    }

    public final Elements elements() {
        return env.elements();
    }

    public final Types types() {
        return env.types();
    }

    public final Filer filer() {
        return env.filer();
    }

    public final void log(Element element, String message, Object... args) {
        env.log(element, message, args);
    }

    public final void log(String message, Object... args) {
        env.log(message, args);
    }

    public final void error(Element element, String message, Object... args) {
        env.error(element, message, args);
    }

    public final void error(String message, Object... args) {
        env.error(message, args);
    }
}
