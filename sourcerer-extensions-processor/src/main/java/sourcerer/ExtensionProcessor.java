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

package sourcerer;

import com.google.auto.service.AutoService;

import java.io.IOException;
import java.util.Set;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import sourcerer.processor.BaseProcessor;

@AutoService(Processor.class)
public class ExtensionProcessor extends BaseProcessor {
    private final Extensions.Processor processor = Extensions.processor();

    @Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        Boolean processed = null;
        boolean write = false;
        for (TypeElement annotationElement : annotations) {
            ExtensionClass extensionClass = annotationElement.getAnnotation(ExtensionClass.class);
            if (extensionClass == null) {
                processed = false;
                continue;
            } else if (processed == null) {
                processed = true;
            }

            Extension.Processor extension = processor.add(extensionClass);
            for (Element typeElement : env.getElementsAnnotatedWith(annotationElement)) {
                // Ensure it is a class element
                if (typeElement.getKind() != ElementKind.CLASS) {
                    error(typeElement, "Only classes can be annotated with @%s", annotationElement.getSimpleName());
                    return true; // Exit processing
                }
                write |= extension.process((TypeElement) typeElement);
            }
        }
        if (processed == null) {
            processed = false;
        }

        if (write) {
            try {
                processor.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                error("error processing %s", processor.extensions());
                return true; // Exit processing
            }
        }
        return processed;
    }

    @Override public Set<String> getSupportedAnnotationTypes() {
        // We need to process all annotation types
        return BaseProcessor.ALL_ANNOTATION_TYPES;
    }
}
