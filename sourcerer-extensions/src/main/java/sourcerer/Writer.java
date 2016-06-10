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
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import okio.BufferedSink;
import okio.ByteString;

public class Writer {
    private final Descriptor.File file;
    private final BufferedSink sink;

    java.io.Reader

    Writer(Descriptor.File file, BufferedSink sink) {
        this.file = file;
        this.sink = sink;
    }

    public Descriptor.File file() {
        return file;
    }

    public Writer writeString(String entry) throws IOException {
        ByteString val = ByteString.encodeUtf8(entry);
        sink.writeInt(val.size());
        sink.write(val);
        return this;
    }

    public <T> Writer writeList(List<T> list, Inker<T> inker) throws IOException {
        int size = list == null ? 0 : list.size();
        sink.writeInt(size);
        for (int i = 0; i < size; i++) {
            inker.pen(this, list.get(i));
        }
        return this;
    }

    public Writer writeStringList(List<String> list) throws IOException {
        return writeList(list, STRING_INK);
    }

    public Writer writeModifiers(Set<Modifier> modifiers) throws IOException {
        return writeList(new ArrayList<>(modifiers), MODIFIER_INK);
    }

    public Writer writeTypeParams(List<? extends TypeParameterElement> typeParams) throws IOException {
        return writeList(ImmutableList.copyOf(typeParams), TYPE_PARAMETER_INK);
    }

    public Writer writeClassName(ClassName className) throws IOException {
        // Write package name
        writeString(className.packageName());

        // Write simple names
        writeStringList(className.simpleNames());

        return this;
    }

    public Writer writeTypeName(TypeName typeName) throws IOException {
        TypeNameKind.write(this, typeName);
        return this;
    }

    public Writer writeTypeNames(List<TypeName> typeNames) throws IOException {
        writeList(typeNames, TypeNameKind.INKER);
        return this;
    }

    public String writeParams(List<? extends VariableElement> params) throws IOException {
        ImmutableList<VariableElement> immutableParams = ImmutableList.copyOf(params);

        // write list
        writeList(immutableParams, PARAM_INK);

        // gather and return param names
        boolean first = true;
        StringBuilder paramString = new StringBuilder();
        for (VariableElement param : immutableParams) {
            String paramName = param.getSimpleName().toString();
            if (!first) {
                paramString.append(", ");
            }
            first = false;
            paramString.append(paramName);
        }
        return paramString.toString();
    }

    public Writer writeAnnotations(List<TypeElement> annotations) throws IOException {
        writeList(annotations, ANNOTATION_INK);
        return this;
    }

    public interface Inker<T> {
        void pen(Writer writer, T t) throws IOException;
    }

    private static final Inker<Modifier> MODIFIER_INK = new Inker<Modifier>() {
        @Override public void pen(Writer writer, Modifier modifier) throws IOException {
            writer.writeString(modifier.name());
        }
    };

    private static final Inker<TypeParameterElement> TYPE_PARAMETER_INK = new Inker<TypeParameterElement>() {
        @Override public void pen(Writer writer, TypeParameterElement typeParam) throws IOException {
            // Write name
            writer.writeString(typeParam.getSimpleName().toString());

            // Write bounds
            writer.writeList(ImmutableList.copyOf(typeParam.getBounds()), TYPE_MIRROR_INK);
        }
    };

    private static final Inker<String> STRING_INK = new Inker<String>() {
        @Override public void pen(Writer writer, String s) throws IOException {
            writer.writeString(s);
        }
    };

    private static final Inker<TypeMirror> TYPE_MIRROR_INK = new Inker<TypeMirror>() {
        @Override public void pen(Writer writer, TypeMirror typeMirror) throws IOException {
            writer.writeTypeName(TypeName.get(typeMirror));
        }
    };

    private static final Inker<AnnotationMirror> ANNOTATION_MIRROR_INK = new Inker<AnnotationMirror>() {
        @Override public void pen(Writer writer, AnnotationMirror am) throws IOException {
            TypeElement te = (TypeElement) am.getAnnotationType().asElement();
            writer.writeClassName(ClassName.get(te));
        }
    };

    private static final Inker<TypeElement> ANNOTATION_INK = new Inker<TypeElement>() {
        @Override public void pen(Writer writer, TypeElement typeElement) throws IOException {
            writer.writeClassName(ClassName.get(typeElement));
        }
    };

    private static final Inker<VariableElement> PARAM_INK = new Inker<VariableElement>() {
        @Override public void pen(Writer writer, VariableElement param) throws IOException {
            // Write name
            writer.writeString(param.getSimpleName().toString());

            // Write type
            TypeName type = TypeName.get(param.asType());
            if (TypeNameKind.write(writer, type)) {
                // Write modifiers
                writer.writeModifiers(param.getModifiers());

                // Write annotations
                writer.writeList(ImmutableList.copyOf(param.getAnnotationMirrors()), ANNOTATION_MIRROR_INK);
            }
        }
    };
}
