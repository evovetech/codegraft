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

import com.google.auto.service.AutoService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

@AutoService(javax.annotation.processing.Processor.class)
public class Processor extends javax.annotation.processing.AbstractProcessor {
    private final List<Template> templates;

    public Processor() {
        List<Template> templates = new ArrayList<>();
        for (Template template : ServiceLoader.load(Template.class)) {
            templates.add(template);
        }
        this.templates = Collections.unmodifiableList(templates);
    }

    @Override public final synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        for (Template template : templates) {
            template.init(env);
        }
    }

    @Override public final boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        boolean processed = false;
        for (Template template : templates) {
            processed |= template.process(annotations, env);
        }
        return processed;
    }

    @Override public final Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new HashSet<>();
        for (Template template : templates) {
            types.addAll(template.supportedAnnotationTypes());
        }
        return types;
    }

    @Override public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
