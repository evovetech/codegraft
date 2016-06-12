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

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;
import com.squareup.javapoet.WildcardTypeName;

import java.io.IOException;
import java.util.List;

public final class TypeNames {
    private static final Reader.Parser<TypeName> TYPE_PARSER = new Reader.Parser<TypeName>() {
        @Override public TypeName parse(Reader reader) throws IOException {
            String name = reader.readString();
            Kind kind = Kind.find(name);
            return kind.parse(reader);
        }
    };
    private static final Writer.Inker<TypeName> TYPE_INKER = new Writer.Inker<TypeName>() {
        @Override public boolean pen(Writer writer, TypeName typeName) throws IOException {
            Kind kind = Kind.find(typeName);
            writer.writeString(kind.name());
            return kind.pen(writer, typeName);
        }
    };

    private TypeNames() { throw new AssertionError("no instances"); }

    public static TypeName read(Reader reader) throws IOException {
        return TYPE_PARSER.parse(reader);
    }

    public static List<TypeName> readList(Reader reader) throws IOException {
        return reader.readList(TYPE_PARSER);
    }

    public static boolean write(Writer writer, TypeName typeName) throws IOException {
        return TYPE_INKER.pen(writer, typeName);
    }

    public static void writeList(Writer writer, List<TypeName> typeNames) throws IOException {
        writer.writeList(typeNames, TYPE_INKER);
    }

    private enum Kind implements ReadWriter<TypeName> {
        Primitive(new ReadWriter<TypeName>() {
            @Override public boolean canWrite(TypeName typeName) {
                return typeName.isPrimitive();
            }

            @Override public TypeName parse(Reader reader) throws IOException {
                ClassName className = reader.readClassName();
                return className.unbox();
            }

            @Override public boolean pen(Writer writer, TypeName typeName) throws IOException {
                ClassName className = (ClassName) typeName.box();
                writer.writeClassName(className);
                return true;
            }
        }),
        Class(new AbstractReadWriter<ClassName>(ClassName.class) {
            @Override public ClassName parse(Reader reader) throws IOException {
                return reader.readClassName();
            }

            @Override protected boolean writeTypeName(Writer writer, ClassName className) throws IOException {
                writer.writeClassName(className);
                return true;
            }
        }),
        Parameterized(new AbstractReadWriter<ParameterizedTypeName>(ParameterizedTypeName.class) {
            @Override public ParameterizedTypeName parse(Reader reader) throws IOException {
                // Read raw type
                ClassName rawType = reader.readClassName();

                // Write type arguments
                List<TypeName> types = readList(reader);

                // newReader type
                return ParameterizedTypeName.get(rawType, types.toArray(new TypeName[types.size()]));
            }

            @Override protected boolean writeTypeName(Writer writer, ParameterizedTypeName ptn) throws IOException {
                // Write raw type
                writer.writeClassName(ptn.rawType);

                // Write type arguments
                writeList(writer, ptn.typeArguments);
                return true;
            }
        }),
        TypeVariable(new AbstractReadWriter<TypeVariableName>(TypeVariableName.class) {
            @Override public TypeVariableName parse(Reader reader) throws IOException {
                // Read name
                String name = reader.readString();

                // Read bounds
                List<TypeName> bounds = readList(reader);

                // Create type
                return TypeVariableName.get(name, bounds.toArray(new TypeName[bounds.size()]));
            }

            @Override protected boolean writeTypeName(Writer writer, TypeVariableName tvn) throws IOException {
                // Write name
                writer.writeString(tvn.name);

                // Write bounds
                writeList(writer, tvn.bounds);
                return true;
            }
        }),
        Array(new AbstractReadWriter<ArrayTypeName>(ArrayTypeName.class) {
            @Override public ArrayTypeName parse(Reader reader) throws IOException {
                TypeName type = read(reader);
                return type == null ? null : ArrayTypeName.of(type);
            }

            @Override protected boolean writeTypeName(Writer writer, ArrayTypeName atn) throws IOException {
                return write(writer, atn.componentType);
            }
        }),
        Wildcard(new AbstractReadWriter<WildcardTypeName>(WildcardTypeName.class) {
            @Override public WildcardTypeName parse(Reader reader) throws IOException {
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

            @Override protected boolean writeTypeName(Writer writer, WildcardTypeName wtn) throws IOException {
                // Write upper bounds
                writeList(writer, wtn.upperBounds);

                // Write lower bounds
                writeList(writer, wtn.lowerBounds);

                return true;
            }
        }),
        Unknown(ReadWriter.EMPTY);

        private final ReadWriter<?> rw;

        Kind(ReadWriter<?> rw) {
            this.rw = rw;
        }

        private static Kind find(String name) {
            for (Kind kind : values()) {
                if (kind.name().equals(name)) {
                    return kind;
                }
            }
            return Unknown;
        }

        private static Kind find(TypeName typeName) {
            for (Kind kind : values()) {
                if (kind.canWrite(typeName)) {
                    return kind;
                }
            }
            // Couldn't write type name
            System.out.print("couldn't find type name -> ");
            Util.log(typeName);
            return Unknown;
        }

        @Override public boolean canWrite(TypeName typeName) {
            return rw.canWrite(typeName);
        }

        @Override public TypeName parse(Reader reader) throws IOException {
            return rw.parse(reader);
        }

        @Override public boolean pen(Writer writer, TypeName typeName) throws IOException {
            return rw.pen(writer, typeName);
        }
    }

    private interface ReadWriter<T extends TypeName> extends Reader.Parser<T>, Writer.Inker<TypeName> {
        ReadWriter<TypeName> EMPTY = new ReadWriter<TypeName>() {
            @Override public boolean canWrite(TypeName typeName) {
                return false;
            }

            @Override public TypeName parse(Reader reader) throws IOException {
                return null;
            }

            @Override public boolean pen(Writer writer, TypeName typeName) throws IOException {
                return false;
            }
        };

        boolean canWrite(TypeName typeName);
    }

    private static abstract class AbstractReadWriter<T extends TypeName> implements ReadWriter<T> {
        private final Class<T> type;

        AbstractReadWriter(Class<T> type) {
            this.type = type;
        }

        abstract boolean writeTypeName(Writer writer, T t) throws IOException;

        @Override public boolean canWrite(TypeName typeName) {
            return type.isInstance(typeName);
        }

        @Override public final boolean pen(Writer writer, TypeName typeName) throws IOException {
            return canWrite(typeName) &&
                    writeTypeName(writer, type.cast(typeName));
        }
    }
}
