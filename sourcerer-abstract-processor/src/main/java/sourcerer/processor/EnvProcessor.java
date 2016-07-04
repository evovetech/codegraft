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

import com.google.common.base.MoreObjects;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;

public abstract class EnvProcessor<E extends Env> extends AbstractProcessor {
    protected static final String ALL_ANNOTATIONS = Env.ALL_ANNOTATIONS;
    protected static final Set<String> ALL_ANNOTATION_TYPES = Env.ALL_ANNOTATION_TYPES;

    private final AtomicReference<E> env = new AtomicReference<>();

    @Override public final synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        env.compareAndSet(null, createEnv(processingEnvironment));
        init(env.get());
    }

    protected abstract E createEnv(ProcessingEnvironment processingEnv);

    protected void init(E e) {
        // subclass override
    }

    @Override public abstract Set<String> getSupportedAnnotationTypes();

    @Override public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    public final E env() {
        return env.get();
    }

    public final void log(Element element, String message, Object... args) {
        Env env = env();
        if (env != null) {
            env.log(element, message, args);
        }
    }

    public final void log(String message, Object... args) {
        Env env = env();
        if (env != null) {
            env.log(message, args);
        }
    }

    public final void error(Element element, String message, Object... args) {
        Env env = env();
        if (env != null) {
            env.error(element, message, args);
        }
    }

    public final void error(String message, Object... args) {
        Env env = env();
        if (env != null) {
            env.error(message, args);
        }
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("supportedAnnotationTypes", getSupportedAnnotationTypes())
                .toString();
    }
}
