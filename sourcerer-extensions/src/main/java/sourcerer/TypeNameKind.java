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
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;
import com.squareup.javapoet.WildcardTypeName;

import java.io.IOException;
import java.util.List;

public enum TypeNameKind {
    Class(ClassName.class) {
        @Override public TypeName readTypeName(Reader reader) throws IOException {
            return reader.readClassName();
        }

        @Override protected boolean writeTypeName(Writer writer, TypeName typeName) throws IOException {
            writer.writeClassName(cast(typeName));
            return true;
        }
    },
    Parameterized(ParameterizedTypeName.class) {
        @Override public TypeName readTypeName(Reader reader) throws IOException {
            // Read raw type
            ClassName rawType = reader.readClassName();

            // Write type arguments
            List<TypeName> types = readList(reader);

            // create type
            return ParameterizedTypeName.get(rawType, types.toArray(new TypeName[types.size()]));
        }

        @Override protected boolean writeTypeName(Writer writer, TypeName typeName) throws IOException {
            ParameterizedTypeName ptn = cast(typeName);

            // Write raw type
            writer.writeClassName(ptn.rawType);

            // Write type arguments
            writeList(writer, ptn.typeArguments);
            return true;
        }
    },
    TypeVariable(TypeVariableName.class) {
        @Override public TypeName readTypeName(Reader reader) throws IOException {
            // Read name
            String name = reader.readString();

            // Read bounds
            List<TypeName> bounds = readList(reader);

            // Create type
            return TypeVariableName.get(name, bounds.toArray(new TypeName[bounds.size()]));
        }

        @Override protected boolean writeTypeName(Writer writer, TypeName typeName) throws IOException {
            TypeVariableName tvn = cast(typeName);

            // Write name
            writer.writeString(tvn.name);

            // Write bounds
            writeList(writer, tvn.bounds);
            return true;
        }
    },
    Array(ArrayTypeName.class) {
        @Override public TypeName readTypeName(Reader reader) throws IOException {
            TypeName type = read(reader);
            return type == null ? null : ArrayTypeName.of(type);
        }

        @Override protected boolean writeTypeName(Writer writer, TypeName typeName) throws IOException {
            ArrayTypeName atn = cast(typeName);
            return write(writer, atn.componentType);
        }
    },
    Wildcard(WildcardTypeName.class) {
        @Override public TypeName readTypeName(Reader reader) throws IOException {
            List<TypeName> upperBounds = readList(reader);
            List<TypeName> lowerBounds = readList(reader);

            if (upperBounds.size() != 1) {
                System.out.println("wrong size for wildcard upper bounds");
                return null;
            }

            TypeName upperBound = upperBounds.get(0);
            if (lowerBounds.isEmpty()) {
                return WildcardTypeName.subtypeOf(upperBound);
            } else if (lowerBounds.size() == 1) {
                if (!upperBound.equals(TypeName.OBJECT)) {
                    System.out.print("upper bound should be object but is -> ");
                    Util.log(upperBound);
                    return null;
                }
                TypeName lowerBound = lowerBounds.get(0);
                return WildcardTypeName.supertypeOf(lowerBound);
            }
            System.out.println("invalid wildcard");
            return null;
        }

        @Override protected boolean writeTypeName(Writer writer, TypeName typeName) throws IOException {
            WildcardTypeName wtn = cast(typeName);

            // Write upper bounds
            writeList(writer, wtn.upperBounds);

            // Write lower bounds
            writeList(writer, wtn.lowerBounds);

            return true;
        }
    },
    Primitive(TypeName.class) {
        @Override public TypeName readTypeName(Reader reader) throws IOException {
            ClassName className = reader.readClassName();
            return className.unbox();
        }

        @Override protected boolean writeTypeName(Writer writer, TypeName typeName) throws IOException {
            ClassName className = (ClassName) typeName.box();
            writer.writeClassName(className);
            return true;
        }
    };

    public static final Reader.Parser<TypeName> PARSER = new Reader.Parser<TypeName>() {
        @Override public TypeName parse(Reader reader) throws IOException {
            return read(reader);
        }
    };

    public static final Writer.Inker<TypeName> INKER = new Writer.Inker<TypeName>() {
        @Override public void pen(Writer writer, TypeName typeName) throws IOException {
            write(writer, typeName);
        }
    };

    private final Class<? extends TypeName> type;

    TypeNameKind(Class<? extends TypeName> type) {
        this.type = type;
    }

    public static TypeName read(Reader reader) throws IOException {
        String name = reader.readString();
        for (TypeNameKind kind : values()) {
            if (kind.name().equalsIgnoreCase(name)) {
                return kind.readTypeName(reader);
            }
        }
        System.out.println("couldn't read type name");
        return null;
    }

    public static boolean write(Writer writer, TypeName typeName) throws IOException {
        TypeNameKind kind = kind(typeName);
        if (kind != null) {
            writer.writeString(kind.name());
            return kind.writeTypeName(writer, typeName);
        }
        // Couldn't write type name
        System.out.print("couldn't write type name -> ");
        Util.log(typeName);
        writer.writeString("");
        return false;
    }

    private static List<TypeName> readList(Reader reader) throws IOException {
        return reader.readList(PARSER);
    }

    private static void writeList(Writer writer, List<TypeName> list) throws IOException {
        writer.writeList(list, INKER);
    }

    private static TypeNameKind kind(TypeName typeName) {
        if (typeName.isPrimitive()) {
            return Primitive;
        }
        for (TypeNameKind kind : values()) {
            if (kind == Primitive) continue;
            if (kind.type.isInstance(typeName)) {
                return kind;
            }
        }
        return null;
    }

    protected abstract TypeName readTypeName(Reader reader) throws IOException;

    protected abstract boolean writeTypeName(Writer writer, TypeName typeName) throws IOException;

    protected final <T extends TypeName> T cast(TypeName typeName) {
        return this.<T>type().cast(typeName);
    }

    protected final <T extends TypeName> Class<T> type() {
        return (Class<T>) type;
    }
}
