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

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;

public abstract class BaseProcessor extends AbstractProcessor {
    protected static final String ALL_ANNOTATIONS = "*";
    protected static final Set<String> ALL_ANNOTATION_TYPES = Collections.singleton(ALL_ANNOTATIONS);

    private final AtomicReference<Env> env = new AtomicReference<>();

    @Override public final synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        env.compareAndSet(null, new Env(processingEnvironment));
        init(env.get());
    }

    protected void init(Env env) {
        // subclass override
    }

    @Override public abstract Set<String> getSupportedAnnotationTypes();

    @Override public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    public final Env env() {
        return env.get();
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
