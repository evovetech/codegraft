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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

final class ExtensionMethod {
    final ExtensionMethodKind kind;
    final ExecutableElement method;
    final List<TypeElement> returnAnnotations;

    private ExtensionMethod(ExtensionMethodKind kind, ExecutableElement method, List<TypeElement> returnAnnotations) {
        if (kind == ExtensionMethodKind.Instance && method.getParameters().size() > 0) {
            throw new IllegalArgumentException("instance method cannot have parameters");
        }
        this.kind = kind;
        this.method = method;
        this.returnAnnotations = Collections.unmodifiableList(returnAnnotations);
    }

    public static ExtensionMethod parse(Element memberElement) {
        ExtensionMethodKind methodKind = null;
        List<TypeElement> others = new ArrayList<>();
        for (AnnotationMirror am : memberElement.getAnnotationMirrors()) {
            ExtensionMethodKind kind = parseAnnotation(am, others);
            if (methodKind != null && kind != null) {
                String format = "Cannot have annotation '%s' when '%s' is already present";
                String message = String.format(format, kind.annotationType, methodKind.annotationType);
                throw new IllegalStateException(message);
            } else if (kind != null) {
                kind.validate(memberElement);
                methodKind = kind;
            }
        }
        return (methodKind == null)
                ? null
                : new ExtensionMethod(methodKind, (ExecutableElement) memberElement, others);
    }

    private static ExtensionMethodKind parseAnnotation(AnnotationMirror am, List<TypeElement> others) {
        TypeElement te = (TypeElement) am.getAnnotationType().asElement();
        String name = te.getQualifiedName().toString();
        for (ExtensionMethodKind annotationKind : ExtensionMethodKind.values()) {
            if (name.equals(annotationKind.annotationType.getCanonicalName())) {
                return annotationKind;
            }
        }
        others.add(te);
        return null;
    }

    String name() {
        return method.getSimpleName().toString();
    }
}
