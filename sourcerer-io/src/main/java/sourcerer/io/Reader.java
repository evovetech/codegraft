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
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Modifier;

import okio.BufferedSource;
import okio.Okio;

public class Reader implements Closeable {
    private final BufferedSource source;

    protected Reader(BufferedSource source) {
        this.source = source;
    }

    public static Reader newReader(InputStream is) {
        return new Reader(Okio.buffer(Okio.source(is)));
    }

    public static Reader newReader(BufferedSource source) {
        return new Reader(source);
    }

    public String readString() throws IOException {
        int length = source.readInt();
        return source.readUtf8(length);
    }

    public <T> List<T> readList(Parser<T> parser) throws IOException {
        int size = source.readInt();
        if (size == 0) {
            return Collections.emptyList();
        }
        ImmutableList.Builder<T> list = ImmutableList.builder();
        for (int i = 0; i < size; i++) {
            T t = parser.parse(this);
            if (t != null) {
                list.add(t);
            }
        }
        return list.build();
    }

    public List<String> readStringList() throws IOException {
        return readList(STRING_PARSER);
    }

    public Set<Modifier> readModifiers() throws IOException {
        return new HashSet<>(readList(MODIFIER_PARSER));
    }

    public List<TypeVariableName> readTypeParams() throws IOException {
        return readList(TYPE_PARAMETER_PARSER);
    }

    public ClassName readClassName() throws IOException {
        String packageName = readString();
        List<String> simpleNames = new ArrayList<>(readStringList());
        String simpleName = simpleNames.remove(0);
        if (simpleNames.size() == 0) {
            return ClassName.get(packageName, simpleName);
        }
        return ClassName.get(packageName, simpleName, simpleNames.toArray(new String[simpleNames.size()]));
    }

    public TypeName readTypeName() throws IOException {
        return SrcType.read(this);
    }

    public List<TypeName> readTypeNames() throws IOException {
        return SrcType.readList(this);
    }

    public List<ParameterSpec> readParams() throws IOException {
        return readList(PARAM_PARSER);
    }

    public List<AnnotationSpec> readAnnotations() throws IOException {
        return readList(ANNOTATION_PARSER);
    }

    @Override public void close() throws IOException {
        source.close();
    }

    public interface Parser<T> {
        T parse(Reader reader) throws IOException;
    }

    private static final Parser<Modifier> MODIFIER_PARSER = new Parser<Modifier>() {
        @Override public Modifier parse(Reader reader) throws IOException {
            String name = reader.readString();
            for (Modifier modifier : Modifier.values()) {
                if (name.equalsIgnoreCase(modifier.name())) {
                    return modifier;
                }
            }
            return null;
        }
    };

    private static final Parser<TypeVariableName> TYPE_PARAMETER_PARSER = new Parser<TypeVariableName>() {
        @Override public TypeVariableName parse(Reader reader) throws IOException {
            // Read name
            String name = reader.readString();

            // Read bounds
            List<TypeName> bounds = reader.readTypeNames();

            // Create type
            return TypeVariableName.get(name, bounds.toArray(new TypeName[bounds.size()]));
        }
    };

    private static final Parser<String> STRING_PARSER = new Parser<String>() {
        @Override public String parse(Reader reader) throws IOException {
            return reader.readString();
        }
    };

    private static final Parser<ClassName> CLASSNAME_PARSER = new Parser<ClassName>() {
        @Override public ClassName parse(Reader reader) throws IOException {
            return reader.readClassName();
        }
    };

    private static final Parser<AnnotationSpec> ANNOTATION_PARSER = new Parser<AnnotationSpec>() {
        @Override public AnnotationSpec parse(Reader reader) throws IOException {
            return AnnotationSpec.builder(reader.readClassName())
                    // TODO: code blocks
                    .build();
        }
    };

    private static final Parser<ParameterSpec> PARAM_PARSER = new Parser<ParameterSpec>() {
        @Override public ParameterSpec parse(Reader reader) throws IOException {
            // Read type
            TypeName type = reader.readTypeName();
            if (type != null) {
                // Read name
                String name = reader.readString();

                // Read modifiers
                Set<Modifier> paramModifiers = reader.readModifiers();

                // Create param spec with modifiers
                return ParameterSpec.builder(type, name)
                        .addModifiers(paramModifiers.toArray(new Modifier[paramModifiers.size()]))
                        // Read annotations
                        .addAnnotations(reader.readList(ANNOTATION_PARSER))
                        // build
                        .build();
            }
            return null;
        }
    };
}
