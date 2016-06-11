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

package sourcerer.io;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
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
import okio.Okio;

import static sourcerer.io.Writeable.Null;

public class Writer implements Closeable, Flushable {
    private final BufferedSink sink;

    protected Writer(BufferedSink sink) {
        this.sink = sink;
    }

    public static Writer newWriter(OutputStream out) {
        return new Writer(Okio.buffer(Okio.sink(out)));
    }

    public static Writer newWriter(BufferedSink sink) {
        return new Writer(sink);
    }

    public Writer write(Writeable writeable) throws IOException {
        writeable.writeTo(this);
        return this;
    }

    public Writer writeString(String entry) throws IOException {
        ByteString val = ByteString.encodeUtf8(entry);
        sink.writeInt(val.size());
        sink.write(val);
        return this;
    }

    public <T extends Writeable> Writer writeList(List<T> list) throws IOException {
        int size = list == null ? 0 : list.size();
        sink.writeInt(size);
        for (int i = 0; i < size; i++) {
            T t = list.get(i);
            if (t == null) {
                Null.writeTo(this);
            } else {
                t.writeTo(this);
            }
        }
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
        SrcType.write(this, typeName);
        return this;
    }

    public Writer writeTypeNames(List<TypeName> typeNames) throws IOException {
        SrcType.writeList(this, typeNames);
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

    @Override public void close() throws IOException {
        sink.close();
    }

    @Override public void flush() throws IOException {
        sink.flush();
    }

    public interface Inker<T> {
        boolean pen(Writer writer, T t) throws IOException;
    }

    private static final Inker<Modifier> MODIFIER_INK = new Inker<Modifier>() {
        @Override public boolean pen(Writer writer, Modifier modifier) throws IOException {
            writer.writeString(modifier.name());
            return true;
        }
    };

    private static final Inker<TypeParameterElement> TYPE_PARAMETER_INK = new Inker<TypeParameterElement>() {
        @Override public boolean pen(Writer writer, TypeParameterElement typeParam) throws IOException {
            // Write name
            writer.writeString(typeParam.getSimpleName().toString());

            // Write bounds
            writer.writeList(ImmutableList.copyOf(typeParam.getBounds()), TYPE_MIRROR_INK);
            return true;
        }
    };

    private static final Inker<String> STRING_INK = new Inker<String>() {
        @Override public boolean pen(Writer writer, String s) throws IOException {
            writer.writeString(s);
            return true;
        }
    };

    private static final Inker<TypeMirror> TYPE_MIRROR_INK = new Inker<TypeMirror>() {
        @Override public boolean pen(Writer writer, TypeMirror typeMirror) throws IOException {
            return SrcType.write(writer, TypeName.get(typeMirror));
        }
    };

    private static final Inker<AnnotationMirror> ANNOTATION_MIRROR_INK = new Inker<AnnotationMirror>() {
        @Override public boolean pen(Writer writer, AnnotationMirror am) throws IOException {
            TypeElement te = (TypeElement) am.getAnnotationType().asElement();
            writer.writeClassName(ClassName.get(te));
            return true;
        }
    };

    private static final Inker<TypeElement> ANNOTATION_INK = new Inker<TypeElement>() {
        @Override public boolean pen(Writer writer, TypeElement typeElement) throws IOException {
            writer.writeClassName(ClassName.get(typeElement));
            return true;
        }
    };

    private static final Inker<VariableElement> PARAM_INK = new Inker<VariableElement>() {
        @Override public boolean pen(Writer writer, VariableElement param) throws IOException {
            // Write type
            if (SrcType.write(writer, TypeName.get(param.asType()))) {
                // Write name
                writer.writeString(param.getSimpleName().toString());

                // Write modifiers
                writer.writeModifiers(param.getModifiers());

                // Write annotations
                writer.writeList(ImmutableList.copyOf(param.getAnnotationMirrors()), ANNOTATION_MIRROR_INK);
                return true;
            }
            return false;
        }
    };
}
