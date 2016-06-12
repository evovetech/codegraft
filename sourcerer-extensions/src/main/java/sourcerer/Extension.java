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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import javax.lang.model.element.TypeElement;

import sourcerer.io.Reader;
import sourcerer.io.Writeable;
import sourcerer.io.Writer;

public final class Extension {
    private final ExtensionClass.Kind kind;
    private final String packageName;
    private final String className;
    private final String qualifiedName;
    private final ClassName typeName;

    private Extension(String kind, ClassName typeName) {
        this(ExtensionClass.Kind.fromName(kind), typeName);
    }

    private Extension(ExtensionClass.Kind kind, ClassName typeName) {
        this(kind, typeName.packageName(), className(typeName), typeName);
    }

    private Extension(ExtensionClass.Kind kind, String packageName, String className) {
        this(kind, packageName, className, typeName(packageName, className));
    }

    private Extension(ExtensionClass.Kind kind, String packageName, String className, ClassName typeName) {
        this.kind = kind;
        this.packageName = packageName;
        this.className = className;
        this.qualifiedName = qualifiedName(typeName);
        this.typeName = typeName;
    }

    static Extension create(ExtensionClass ext) {
        return new Extension(ext.kind(), ext.packageName(), ext.className());
    }

    public Processor newProcessor() {
        return new Processor();
    }

    private static String className(ClassName typeName) {
        return className(typeName.simpleNames());
    }

    private static String className(List<String> simpleNames) {
        return Joiner.on('.')
                .join(simpleNames);
    }

    private static ClassName typeName(String packageName, String className) {
        if (className.isEmpty()) throw new IllegalArgumentException("empty className");
        int index = className.indexOf('.');
        if (index == -1) {
            return ClassName.get(packageName, className);
        }

        // Add the class names, like "Map" and "Entry".
        String[] parts = className.substring(index + 1).split("\\.", -1);
        return ClassName.get(packageName, className, parts);
    }

    private static String qualifiedName(ClassName typeName) {
        String packageName = typeName.packageName();
        if (packageName.isEmpty()) {
            return className(typeName);
        }
        List<String> names = new ArrayList<>(typeName.simpleNames());
        names.add(0, packageName);
        return Joiner.on('.')
                .join(names);
    }

    public ExtensionClass.Kind kind() {
        return kind;
    }

    public String packageName() {
        return packageName;
    }

    public String className() {
        return className;
    }

    public String qualifiedName() {
        return qualifiedName;
    }

    public ClassName typeName() {
        return typeName;
    }

    public String javaPackagePath() {
        return packageName().replace('.', '/');
    }

    public String javaFileName() {
        return className() + ".java";
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Extension)) return false;

        Extension key = (Extension) o;

        if (kind != key.kind) return false;
        return qualifiedName.equals(key.qualifiedName);
    }

    @Override public int hashCode() {
        int result = kind.hashCode();
        result = 31 * result + qualifiedName.hashCode();
        return result;
    }

    public final class Processor implements Writeable {
        private final LinkedHashSet<ExtensionClassHelper> classHelpers;

        private Processor() {
            this.classHelpers = new LinkedHashSet<>();
        }

        public Extension extension() {
            return Extension.this;
        }

        public boolean process(TypeElement typeElement) {
            ExtensionClassHelper classHelper = ExtensionClassHelper.process(kind(), typeElement);
            System.out.printf("\nparsed class: %s, extClass: %s\n", typeElement, classHelper);
            boolean result;
            synchronized (classHelpers) {
                result = classHelpers.add(classHelper);
            }
            System.out.printf("\nthis = %s\n", this);
            return result;
        }

        @Override public void writeTo(Writer writer) throws IOException {
            // Write extension
            writer.writeString(kind.name());
            writer.writeClassName(typeName);

            List<ExtensionClassHelper> list;
            synchronized (classHelpers) {
                list = new ArrayList<>(classHelpers);
            }
            writer.writeList(list);
        }
    }

    public static final class Sourcerer {
        private static final Reader.Parser<Sourcerer> PARSER = new Reader.Parser<Sourcerer>() {
            @Override public Sourcerer parse(Reader reader) throws IOException {
                String kind = reader.readString();
                ClassName typeName = reader.readClassName();
                Extension extension = new Extension(kind, typeName);
                Reader.Parser<MethodSpec> parser = ExtensionClassHelper.methodParser(typeName);
                List<MethodSpec> methods = reader.readList(parser);
                return new Sourcerer(extension, methods);
            }
        };

        private final Extension extension;
        private final ImmutableList<MethodSpec> methods;

        private Sourcerer(Extension extension, Collection<MethodSpec> methods) {
            this.extension = extension;
            this.methods = ImmutableList.copyOf(methods);
        }

        public static Sourcerer create(Extension extension, Collection<MethodSpec> methods) {
            return new Sourcerer(extension, methods);
        }

        public static Sourcerer read(Reader reader) throws IOException {
            return PARSER.parse(reader);
        }

        public static List<Sourcerer> readList(Reader reader) throws IOException {
            return reader.readList(PARSER);
        }

        public Extension extension() {
            return extension;
        }

        public ImmutableList<MethodSpec> methods() {
            return methods;
        }

        public SourceWriter newSourceWriter() {
            return new SourceWriter(this);
        }
    }
}
