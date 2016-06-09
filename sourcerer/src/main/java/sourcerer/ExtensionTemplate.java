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
import com.google.common.base.MoreObjects;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import okio.BufferedSink;
import okio.BufferedSource;
import sourcerer.processor.Template;

@AutoService(Template.class)
public class ExtensionTemplate extends Template {
    private final Map<ExtensionDescriptor, Type> extensions;

    public ExtensionTemplate() {
        this.extensions = new HashMap<>();
    }

    @Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        boolean processed = false;
        for (TypeElement annotationElement : annotations) {
            Extension extension = annotationElement.getAnnotation(Extension.class);
            if (extension == null) {
                continue;
            }

            Type type = addExtensionType(extension, annotationElement);
            for (Element typeElement : env.getElementsAnnotatedWith(annotationElement)) {
                // Ensure it is a class element
                if (typeElement.getKind() != ElementKind.CLASS) {
                    error(typeElement, "Only classes can be annotated with @%s", annotationElement.getSimpleName());
                    return true; // Exit processing
                }
                type.process((TypeElement) typeElement);
            }
            processed = true;
        }

        if (processed) {
            List<Type> types;
            synchronized (extensions) {
                types = new ArrayList<>(extensions.values());
            }
            for (Type type : types) {
                try {
                    System.out.printf("\nwriting type: %s\n", type.annotationType);
                    type.write(env().processingEnvironment().getFiler());
                } catch (IOException e) {
                    error(type.annotationType, "error processing");
                    return true; // Exit processing
                }
            }
        }
        return processed;
    }

    @Override public Set<String> supportedAnnotationTypes() {
        // We need to process all annotation types
        return Collections.singleton("*");
    }

    private Type addExtensionType(Extension extension, TypeElement annotationElement) {
        final ExtensionDescriptor key
                = new ExtensionDescriptor(extension.kind(), extension.packageName(), extension.className());
        synchronized (extensions) {
            Type ext = extensions.get(key);
            if (ext == null) {
                System.out.printf("\ncreating type for annotation: %s\n", annotationElement);
                ext = new Type(annotationElement, key);
                extensions.put(key, ext);
            }
            return ext;
        }
    }

    private static final class Type extends ExtensionType {
        private static final String OUTPUT_DIR = "META-INF/sourcerer";
        private static final String FILE_EXTENSION = ".sourcerer";

        private final TypeElement annotationType;
        private final LinkedHashSet<ExtensionClass> extensionClasses = new LinkedHashSet<>();

        private Type(TypeElement annotationType, ExtensionDescriptor descriptor) {
            super(descriptor, OUTPUT_DIR, FILE_EXTENSION);
            this.annotationType = annotationType;
        }

        @Override public List<ExtensionClass> extensionClasses() {
            synchronized (extensionClasses) {
                return new ArrayList<>(extensionClasses);
            }
        }

        private void process(TypeElement typeElement) {
            ExtensionClass extensionClass = ExtensionClass.parse(descriptor().kind(), typeElement);
            System.out.printf("\nparsed class: %s, extClass: %s\n", typeElement, extensionClass);
            synchronized (extensionClasses) {
                extensionClasses.add(extensionClass);
            }
            System.out.printf("\nthis = %s\n", this);
        }

        @Override
        protected void readExtension(BufferedSource source, TypeSpec.Builder classBuilder) throws IOException {
            ExtensionClass.readMethods(source, this, classBuilder);
        }

        @Override protected void writeExtension(BufferedSink sink, ExtensionClass extensionClass) throws IOException {
            if (descriptor().kind() == Source.Kind.StaticDelegate) {
                extensionClass.writeMethods(sink, Modifier.STATIC);
            } else {
                extensionClass.writeMethods(sink);
            }
        }

        @Override public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("annotationType", annotationType)
                    .add("extensionClasses", extensionClasses())
                    .toString();
        }
    }
}
