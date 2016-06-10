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

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;
import com.squareup.javapoet.WildcardTypeName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import okio.BufferedSink;
import okio.BufferedSource;
import okio.ByteString;

final class Util {
    private Util() { throw new AssertionError("no instances"); }

    static void writeUtf8Entry(BufferedSink sink, String entry) throws IOException {
        ByteString val = ByteString.encodeUtf8(entry);
        sink.writeInt(val.size());
        sink.write(val);
    }

    static String readUtf8Entry(BufferedSource source) throws IOException {
        int length = source.readInt();
        return source.readUtf8(length);
    }

    static void writeModifiers(BufferedSink sink, Set<Modifier> modifiers) throws IOException {
        List<Modifier> modifierList = new ArrayList<>(modifiers);
        int size = modifierList.size();
        sink.writeInt(size);
        for (int i = 0; i < size; i++) {
            writeModifier(sink, modifierList.get(i));
        }
    }

    static Set<Modifier> readModifiers(BufferedSource source) throws IOException {
        Set<Modifier> modifiers = new HashSet<>();
        int size = source.readInt();
        for (int i = 0; i < size; i++) {
            Modifier modifier = readModifier(source);
            if (modifier != null) {
                modifiers.add(modifier);
            }
        }
        return modifiers;
    }

    static void writeTypeParams(BufferedSink sink, List<? extends TypeParameterElement> typeParams)
            throws IOException {
        int size = typeParams.size();
        sink.writeInt(size);
        for (int i = 0; i < size; i++) {
            TypeParameterElement typeParam = typeParams.get(i);

            // Write name
            writeUtf8Entry(sink, typeParam.getSimpleName().toString());

            // Write bounds
            List<? extends TypeMirror> bounds = typeParam.getBounds();
            int boundsSize = bounds.size();
            sink.writeInt(boundsSize);
            for (int j = 0; j < boundsSize; j++) {
                TypeName typeName = TypeName.get(bounds.get(i));
                writeTypeName(sink, typeName);
            }
        }
    }

    static void readTypeParams(BufferedSource source, MethodSpec.Builder methodBuilder) throws IOException {
        int size = source.readInt();
        for (int i = 0; i < size; i++) {
            // Read name
            String name = readUtf8Entry(source);

            // Read bounds
            List<TypeName> bounds = new ArrayList<>();
            int boundsSize = source.readInt();
            for (int j = 0; j < boundsSize; j++) {
                TypeName typeName = readTypeName(source);
                if (typeName != null) {
                    bounds.add(typeName);
                }
            }
            TypeVariableName typeName = TypeVariableName.get(name, bounds.toArray(new TypeName[bounds.size()]));
            methodBuilder.addTypeVariable(typeName);
        }
    }

    static String writeParams(BufferedSink sink, List<? extends VariableElement> params) throws IOException {
        int size = params.size();
        sink.writeInt(size);

        List<String> paramNames = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            VariableElement param = params.get(i);

            // Write name
            String name = param.getSimpleName().toString();
            writeUtf8Entry(sink, name);
            paramNames.add(name);

            TypeName type = TypeName.get(param.asType());
            if (writeTypeName(sink, type)) {
                // Write modifiers
                writeModifiers(sink, param.getModifiers());

                // Write annotations
                List<? extends AnnotationMirror> annotationMirrors = param.getAnnotationMirrors();
                int annotationSize = annotationMirrors.size();
                sink.writeInt(annotationSize);
                for (int j = 0; j < annotationSize; j++) {
                    AnnotationMirror am = annotationMirrors.get(j);
                    TypeElement te = (TypeElement) am.getAnnotationType().asElement();
                    writeClassName(sink, ClassName.get(te));
                }
            }
        }
        boolean first = true;
        StringBuilder paramString = new StringBuilder();
        for (String paramName : paramNames) {
            if (!first) {
                paramString.append(", ");
            }
            first = false;
            paramString.append(paramName);
        }
        return paramString.toString();
    }

    static void readParams(BufferedSource source, MethodSpec.Builder methodBuilder) throws IOException {
        int size = source.readInt();

        for (int i = 0; i < size; i++) {
            // Read name
            String name = readUtf8Entry(source);

            TypeName type = readTypeName(source);
            if (type != null) {
                // Read modifiers
                Set<Modifier> paramModifiers = readModifiers(source);

                // Create param spec with modifiers
                ParameterSpec.Builder paramSpec = ParameterSpec.builder(type, name)
                        .addModifiers(paramModifiers.toArray(new Modifier[paramModifiers.size()]));

                // Read annotations
                int annotationSize = source.readInt();
                for (int j = 0; j < annotationSize; j++) {
                    ClassName className = readClassName(source);
                    paramSpec.addAnnotation(className);
                }

                methodBuilder.addParameter(paramSpec.build());
            }
        }
    }

    static void writeAnnotations(BufferedSink sink, ExtensionMethodKind kind, List<TypeElement> annotations)
            throws IOException {
        List<ClassName> classNames = new ArrayList<>();
        for (TypeElement annotation : annotations) {
            ClassName className = ClassName.get(annotation);
        }
        int size = classNames.size();
        sink.writeInt(size);
        for (int i = 0; i < size; i++) {
            ClassName className = classNames.get(i);
            writeClassName(sink, className);
        }
    }

    static void readAnnotations(BufferedSource source, MethodSpec.Builder methodBuilder) throws IOException {
        int size = source.readInt();
        for (int i = 0; i < size; i++) {
            ClassName className = readClassName(source);
            methodBuilder.addAnnotation(className);
        }
    }

    static void writeClassName(BufferedSink sink, ClassName className) throws IOException {
        // Write package name
        writeUtf8Entry(sink, className.packageName());

        // Write simple names
        List<String> simpleNames = className.simpleNames();
        int size = simpleNames.size();
        sink.writeInt(size);
        for (int i = 0; i < size; i++) {
            String name = simpleNames.get(i);
            writeUtf8Entry(sink, name);
        }
    }

    static ClassName readClassName(BufferedSource source) throws IOException {
        String packageName = readUtf8Entry(source);
        int size = source.readInt();
        String simpleName = "";
        List<String> simpleNames = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            String name = readUtf8Entry(source);
            if (i == 0) {
                simpleName = name;
            } else {
                simpleNames.add(name);
            }
        }
        if (simpleNames.size() == 0) {
            return ClassName.get(packageName, simpleName);
        }
        return ClassName.get(packageName, simpleName, simpleNames.toArray(new String[simpleNames.size()]));
    }

    static boolean writeTypeName(BufferedSink sink, TypeName typeName) throws IOException {
        if (typeName instanceof ClassName) {
            TypeNameKind.Class.write(sink);
            writeClassName(sink, (ClassName) typeName);
            return true;
        }
        if (typeName instanceof ParameterizedTypeName) {
            TypeNameKind.Parameterized.write(sink);
            ParameterizedTypeName ptn = (ParameterizedTypeName) typeName;

            // Write raw type
            writeClassName(sink, ptn.rawType);

            // Write parameterized types
            List<TypeName> types = ptn.typeArguments;
            int size = types.size();
            sink.writeInt(size);
            for (int i = 0; i < size; i++) {
                TypeName type = types.get(i);
                writeTypeName(sink, type);
            }
            return true;
        }
        if (typeName instanceof TypeVariableName) {
            TypeNameKind.TypeVariable.write(sink);
            TypeVariableName tvn = (TypeVariableName) typeName;

            // Write name
            writeUtf8Entry(sink, tvn.name);

            // Write bounds
            List<TypeName> bounds = tvn.bounds;
            int size = bounds.size();
            sink.writeInt(size);
            for (int i = 0; i < size; i++) {
                TypeName type = bounds.get(i);
                writeTypeName(sink, type);
            }
            return true;
        }
        if (typeName instanceof ArrayTypeName) {
            TypeNameKind.Array.write(sink);
            ArrayTypeName atn = (ArrayTypeName) typeName;
            writeTypeName(sink, atn.componentType);
            return true;
        }
        if (typeName instanceof WildcardTypeName) {
            TypeNameKind.Wildcard.write(sink);
            WildcardTypeName wtn = (WildcardTypeName) typeName;

            // Write upper bounds
            int size = wtn.upperBounds.size();
            sink.writeInt(size);
            for (int i = 0; i < size; i++) {
                TypeName type = wtn.upperBounds.get(i);
                writeTypeName(sink, type);
            }

            // Write lower bounds
            size = wtn.lowerBounds.size();
            sink.writeInt(size);
            for (int i = 0; i < size; i++) {
                TypeName type = wtn.lowerBounds.get(i);
                writeTypeName(sink, type);
            }
            return true;
        }
        if (typeName.isPrimitive()) {
            TypeNameKind.Primitive.write(sink);
            ClassName className = (ClassName) typeName.box();
            writeClassName(sink, className);
            return true;
        }

        // Couldn't write type name
        System.out.print("couldn't write type name -> ");
        log(typeName);
        writeUtf8Entry(sink, "");
        return false;
    }

    static TypeName readTypeName(BufferedSource source) throws IOException {
        TypeNameKind kind = TypeNameKind.read(source);
        if (kind == null) {
            System.out.println("no type name found");
            return null;
        }
        switch (kind) {
            case Class: {
                return readClassName(source);
            }
            case Parameterized: {
                ClassName rawType = readClassName(source);
                List<TypeName> types = new ArrayList<>();
                int size = source.readInt();
                for (int i = 0; i < size; i++) {
                    TypeName type = readTypeName(source);
                    if (type != null) {
                        types.add(type);
                    }
                }
                return ParameterizedTypeName.get(rawType, types.toArray(new TypeName[types.size()]));
            }
            case TypeVariable: {
                String name = readUtf8Entry(source);
                List<TypeName> bounds = new ArrayList<>();
                int size = source.readInt();
                for (int i = 0; i < size; i++) {
                    TypeName bound = readTypeName(source);
                    if (bound != null) {
                        bounds.add(bound);
                    }
                }
                return TypeVariableName.get(name, bounds.toArray(new TypeName[bounds.size()]));
            }
            case Array: {
                TypeName type = readTypeName(source);
                if (type != null) {
                    return ArrayTypeName.of(type);
                }
                break;
            }
            case Wildcard: {
                List<TypeName> upperBounds = new ArrayList<>();
                List<TypeName> lowerBounds = new ArrayList<>();

                // Read upper bounds
                int size = source.readInt();
                for (int i = 0; i < size; i++) {
                    TypeName bound = readTypeName(source);
                    if (bound != null) {
                        upperBounds.add(bound);
                    }
                }

                // Read lower bounds
                size = source.readInt();
                for (int i = 0; i < size; i++) {
                    TypeName bound = readTypeName(source);
                    if (bound != null) {
                        lowerBounds.add(bound);
                    }
                }

                if (upperBounds.size() != 1) {
                    System.out.println("wrong size for wildcard upper bounds");
                    break;
                }
                TypeName upperBound = upperBounds.get(0);
                if (lowerBounds.isEmpty()) {
                    return WildcardTypeName.subtypeOf(upperBound);
                } else if (lowerBounds.size() == 1) {
                    if (!upperBound.equals(TypeName.OBJECT)) {
                        System.out.print("upper bound should be object but is -> ");
                        log(upperBound);
                        break;
                    }
                    TypeName lowerBound = lowerBounds.get(0);
                    return WildcardTypeName.supertypeOf(lowerBound);
                }
                System.out.println("invalid wildcard");
                break;
            }
            case Primitive: {
                ClassName className = readClassName(source);
                return className.unbox();
            }
        }
        System.out.println("couldn't read type name");
        return null;
    }

    private static void writeModifier(BufferedSink sink, Modifier modifier) throws IOException {
        writeUtf8Entry(sink, modifier.name());
    }

    private static Modifier readModifier(BufferedSource source) throws IOException {
        String name = readUtf8Entry(source);
        for (Modifier modifier : Modifier.values()) {
            if (name.equalsIgnoreCase(modifier.name())) {
                return modifier;
            }
        }
        return null;
    }

    static void log(TypeName typeName) {
        System.out.println("typeName: " + typeName + ", class: " + typeName.getClass());
    }

    private enum TypeNameKind {
        Class,
        Parameterized,
        TypeVariable,
        Array,
        Wildcard,
        Primitive;

        private void write(BufferedSink sink) throws IOException {
            writeUtf8Entry(sink, name());
        }

        private static TypeNameKind read(BufferedSource source) throws IOException {
            String name = readUtf8Entry(source);
            for (TypeNameKind kind : values()) {
                if (kind.name().equalsIgnoreCase(name)) {
                    return kind;
                }
            }
            return null;
        }
    }
}
