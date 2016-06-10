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

import com.google.common.base.MoreObjects;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

final class ExtensionClassHelper {
    private final ExtensionClass.Kind kind;
    private final TypeElement element;
    private final ExtensionMethodHelper instanceMethod;
    private final List<ExtensionMethodHelper> methods;

    private ExtensionClassHelper(ExtensionClass.Kind kind, TypeElement element, ExtensionMethodHelper instanceMethod,
            List<ExtensionMethodHelper> methods) {
        if (instanceMethod == null) {
            throw new IllegalArgumentException(element.getQualifiedName() + " must have an instance method specified");
        } else if (!ClassName.get(element).equals(TypeName.get(instanceMethod.method.getReturnType()))) {
            throw new IllegalArgumentException(
                    element.getQualifiedName() + " instance method must return its own type");
        } else if (methods.size() == 0) {
            throw new IllegalArgumentException(element.getQualifiedName() + " has no annotated methods to process");
        }
        this.kind = kind;
        this.element = element;
        this.instanceMethod = instanceMethod;
        this.methods = Collections.unmodifiableList(methods);
    }

    static ExtensionClassHelper parse(ExtensionClass.Kind kind, TypeElement element) {
        ExtensionMethodHelper instanceMethod = null;
        List<ExtensionMethodHelper> methods = new ArrayList<>();
        for (Element memberElement : element.getEnclosedElements()) {
            ExtensionMethodHelper method = ExtensionMethodHelper.parse(memberElement);
            if (method == null) continue;
            switch (method.kind) {
                case ExtensionMethodKind.Instance:
                    if (instanceMethod != null) {
                        String format = "Cannot have instance method '%s' when '%s' is already defined";
                        String message = String.format(format, method.name(), instanceMethod.name());
                        throw new IllegalStateException(message);
                    }
                    instanceMethod = method;
                    break;
                default:
                    methods.add(method);
                    break;
            }
        }
        return new ExtensionClassHelper(kind, element, instanceMethod, methods);
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExtensionClassHelper that = (ExtensionClassHelper) o;

        return element.getQualifiedName().toString().equals(that.element.getQualifiedName().toString());
    }

    @Override public int hashCode() {
        return element.getQualifiedName().toString().hashCode();
    }

    void writeMethods(Writer writer, Modifier... modifiers) throws IOException {
        writer.writeList(methods, new MethodInk(modifiers));
    }

    static List<MethodSpec> readMethods(Reader reader, ExtensionType kind) throws IOException {
        return reader.readList(new MethodParser(kind));
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("element", element)
                .add("kind", kind)
                .toString();
    }

    private final class MethodInk implements Writer.Inker<ExtensionMethodHelper> {
        private final Modifier[] modifiers;

        private MethodInk(Modifier[] modifiers) {
            this.modifiers = modifiers;
        }

        @Override public void pen(Writer writer, ExtensionMethodHelper extensionMethodHelper) throws IOException {
            ExecutableElement methodElement = extensionMethodHelper.method;

            // Write method name
            String methodName = methodElement.getSimpleName().toString();
            writer.writeString(methodName);

            // Write modifiers
            Set<Modifier> mods = new HashSet<>(methodElement.getModifiers());
            mods.addAll(Arrays.asList(modifiers));
            writer.writeModifiers(mods);

            // Write type parameters
            writer.writeTypeParams(methodElement.getTypeParameters());

            // Write parameters
            String params = writer.writeParams(methodElement.getParameters());

            // Write return annotations
            writer.writeAnnotations(new ArrayList<>(extensionMethodHelper.returnAnnotations));

            // Write classType
            writer.writeClassName(ClassName.get(element));

            // Write statement
            String statement = String.format("$T.%s().%s(%s)", instanceMethod.name(), extensionMethodHelper.name(),
                    params);
            writer.writeString(statement);

            // Write method kind
            ExtensionMethodKind methodKind = extensionMethodHelper.kind;
            writer.writeString(methodKind.name());
            switch (methodKind) {
                case ExtensionMethodKind.Return:
                    TypeName returnType = TypeName.get(methodElement.getReturnType());
                    writer.writeTypeName(returnType);
                    break;
                case ExtensionMethodKind.ReturnThis:
                case ExtensionMethodKind.Void:
                    break;
                default:
                    throw new IllegalStateException("invalid method kind");
            }
        }
    }

    private static final class MethodParser implements Reader.Parser<MethodSpec> {
        private final ExtensionType kind;

        private MethodParser(ExtensionType kind) {
            this.kind = kind;
        }

        @Override public MethodSpec parse(Reader reader) throws IOException {
            // Read method name
            String methodName = reader.readString();

            // Read modifiers
            Set<Modifier> modifiers = reader.readModifiers();

            // Create method builder with modifiers
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName)
                    .addModifiers(modifiers.toArray(new Modifier[modifiers.size()]))
                    // Read type parameters
                    .addTypeVariables(reader.readTypeParams())
                    // Read Parameters
                    .addParameters(reader.readParams())
                    // Read return annotations
                    .addAnnotations(reader.readAnnotations());

            // Read classType
            ClassName classType = reader.readClassName();

            // Read statement
            String statement = reader.readString();

            // Read method kind
            ExtensionMethodKind methodKind = ExtensionMethodKind.read(reader);
            switch (methodKind) {
                case ExtensionMethodKind.Return:
                    TypeName returnType = reader.readTypeName();
                    methodBuilder.returns(returnType);
                    statement = "return " + statement;
                    methodBuilder.addStatement(statement, classType);
                    break;
                case ExtensionMethodKind.ReturnThis:
                    methodBuilder.returns(kind.descriptor().typeName());
                    methodBuilder.addStatement(statement, classType);
                    methodBuilder.addStatement("return this");
                    break;
                case ExtensionMethodKind.Void:
                    methodBuilder.addStatement(statement, classType);
                    break;
                default:
                    throw new IllegalStateException("invalid method kind");
            }

            // build
            return methodBuilder.build();
        }
    }
}
