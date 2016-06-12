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

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

final class ExtensionMethodHelper {
    static final Class<ExtensionMethod> ANNOTATION_TYPE = ExtensionMethod.class;
    static final String ANNOTATION_NAME = ANNOTATION_TYPE.getCanonicalName();

    final ExtensionMethod.Kind kind;
    final ExecutableElement method;
    final ImmutableList<AnnotationMirror> returnAnnotations;

    private ExtensionMethodHelper(ExtensionMethod.Kind kind, ExecutableElement method,
            List<AnnotationMirror> returnAnnotations) {
        if (kind == ExtensionMethod.Kind.Instance && method.getParameters().size() > 0) {
            throw new IllegalArgumentException("instance method cannot have parameters");
        }
        this.kind = kind;
        this.method = method;
        this.returnAnnotations = ImmutableList.copyOf(returnAnnotations);
    }

    public static ExtensionMethodHelper process(Element memberElement) {
        ExtensionMethod.Kind methodKind = null;
        List<AnnotationMirror> others = new ArrayList<>();
        for (AnnotationMirror am : memberElement.getAnnotationMirrors()) {
            ExtensionMethod.Kind kind = parseAnnotation(memberElement, am, others);
            if (methodKind != null && kind != null) {
                String format = "Cannot have annotation '%s' when it is already present";
                String message = String.format(format, ANNOTATION_TYPE);
                throw new IllegalStateException(message);
            } else if (kind != null) {
                validate(kind, memberElement);
                methodKind = kind;
            }
        }
        return (methodKind == null)
                ? null
                : new ExtensionMethodHelper(methodKind, (ExecutableElement) memberElement, others);
    }

    String name() {
        return method.getSimpleName().toString();
    }

    private static ExtensionMethod.Kind parseAnnotation(Element memberElement, AnnotationMirror am,
            List<AnnotationMirror> others) {
        TypeElement te = (TypeElement) am.getAnnotationType().asElement();
        String name = te.getQualifiedName().toString();
        if (ANNOTATION_NAME.equals(name)) {
            ExtensionMethod method = memberElement.getAnnotation(ANNOTATION_TYPE);
            return method.value();
        }
        others.add(am);
        return null;
    }

    private static void validate(ExtensionMethod.Kind kind, Element element) {
        String name = element.getSimpleName().toString();
        String format = String.format("'%s' element with method kind '%s' %s", name, kind, "%s");
        if (element.getKind() != ElementKind.METHOD) {
            String message = String.format(format, "must be a method");
            throw new IllegalArgumentException(message);
        }
        Set<Modifier> modifiers = element.getModifiers();
        if (!modifiers.contains(Modifier.PUBLIC)) {
            String message = String.format(format, "must be public");
            throw new IllegalArgumentException(message);
        }
        if (kind == ExtensionMethod.Kind.Instance) {
            if (!modifiers.contains(Modifier.STATIC)) {
                String message = "Instance Method " + String.format(format, "must be static");
                throw new IllegalArgumentException(message);
            }
        } else if (modifiers.contains(Modifier.STATIC)) {
            String message = String.format(format, "must not be static");
            throw new IllegalArgumentException(message);
        }
    }
}
