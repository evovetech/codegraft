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

import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;

public abstract class TemplateProcessor<T extends Template> extends EnvProcessor<T> {
    @Override public final Set<String> getSupportedAnnotationTypes() {
        return env().supportedAnnotationTypes();
    }

    @Override public final boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return env().process(annotations, roundEnv);
    }
}
