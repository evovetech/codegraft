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
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
    private final Set<String> annotationTypes;
    private final Map<ExtensionDescriptor, Type> extensions;

    public ExtensionTemplate() {
        this.annotationTypes = new HashSet<>();
        this.extensions = new HashMap<>();
        annotationTypes.add(Extension.class.getCanonicalName());
    }

    @Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        boolean processed = false;
        for (Element annotationElement : env.getElementsAnnotatedWith(Extension.class)) {
            // Ensure it is an annotation element
            if (annotationElement.getKind() != ElementKind.ANNOTATION_TYPE) {
                error(annotationElement, "Only annotations can be annotated with @%s", Extension.class.getSimpleName());
                return true; // Exit processing
            }

            Type type = addExtensionType((TypeElement) annotationElement);
            TypeElement typeAnnotation = (TypeElement) annotationElement;
            synchronized (annotationTypes) {
                annotationTypes.add(typeAnnotation.getQualifiedName().toString());
            }
            for (Element typeElement : env.getElementsAnnotatedWith(typeAnnotation)) {
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
        synchronized (annotationTypes) {
            return new HashSet<>(annotationTypes);
        }
    }

    private Type addExtensionType(TypeElement annotationElement) {
        final Extension extension = annotationElement.getAnnotation(Extension.class);
        final ExtensionDescriptor key
                = new ExtensionDescriptor(extension.kind(), extension.packageName(), extension.className());
        synchronized (extensions) {
            Type ext = extensions.get(key);
            if (ext == null) {
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
            System.out.printf("\nparsed type: %s, extClass: %s\n", typeElement, extensionClass);
            synchronized (extensionClasses) {
                extensionClasses.add(extensionClass);
            }
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
    }
}
