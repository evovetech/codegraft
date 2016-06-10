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

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;

enum ExtensionMethodKind {
    Instance(sourcerer.InstanceMethod.class),
    Void(sourcerer.VoidMethod.class),
    Return(sourcerer.ReturnMethod.class),
    ReturnThis(sourcerer.ReturnThisMethod.class);

    final Class<? extends Annotation> annotationType;

    ExtensionMethodKind(Class<? extends Annotation> annotationType) {
        this.annotationType = annotationType;
    }

    static ExtensionMethodKind read(Reader reader) throws IOException {
        String name = reader.readString();
        for (ExtensionMethodKind kind : values()) {
            if (name.equalsIgnoreCase(kind.name())) {
                return kind;
            }
        }
        throw new IllegalStateException("invalid method kind: " + name);
    }

    void write(Writer writer) throws IOException {
        writer.writeString(name());
    }

    void validate(Element element) {
        String name = element.getSimpleName().toString();
        String format = String.format("'%s' element with annotation '%s' %s", name, annotationType, "%s");
        if (element.getKind() != ElementKind.METHOD) {
            String message = String.format(format, "must be a method");
            throw new IllegalArgumentException(message);
        }
        Set<Modifier> modifiers = element.getModifiers();
        if (!modifiers.contains(Modifier.PUBLIC)) {
            String message = String.format(format, "must be public");
            throw new IllegalArgumentException(message);
        }
        if (this == Instance) {
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
