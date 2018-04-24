/*
 * Copyright 2018 evove.tech
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

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import java.util.Set;

public abstract
class Template
        extends Env
{
    protected
    Template(Template template) {
        super(template);
    }

    protected
    Template(Env env) {
        super(env);
    }

    protected
    Template(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    public abstract
    Set<String> supportedAnnotationTypes();

    public abstract
    boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv);
}
