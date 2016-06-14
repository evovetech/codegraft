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
import com.google.common.collect.ImmutableMap;
import com.squareup.javapoet.MethodSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.jar.JarInputStream;

import javax.annotation.processing.Filer;

import okio.BufferedSink;
import sourcerer.io.Reader;
import sourcerer.io.Writer;

public final class Extensions {
    private static final String DIR = "ext";
    private static final String FILE_NAME = "extensions";

    private static final Extensions INSTANCE = new Extensions();

    private final MetaInf.File file;

    private Extensions() {
        this.file = MetaInf.file(DIR, FILE_NAME);
    }

    public static Processor processor() {
        return INSTANCE.newProcessor();
    }

    public static Extensions instance() {
        return INSTANCE;
    }

    public MetaInf.File file() {
        return file;
    }

    public Processor newProcessor() {
        return new Processor();
    }

    public final class Processor {
        private final LinkedHashSet<Extension.Processor> extensions;

        private Processor() {
            this.extensions = new LinkedHashSet<>();
        }

        public Extensions extensions() {
            return Extensions.this;
        }

        public Extension.Processor add(ExtensionClass extensionClass) {
            Extension ext = Extension.create(extensionClass);
            synchronized (extensions) {
                for (Extension.Processor next : extensions) {
                    if (ext.equals(next.extension())) {
                        return next;
                    }
                }
                Extension.Processor processor = ext.newProcessor();
                extensions.add(processor);
                return processor;
            }
        }

        /* visible for testing */
        final void writeTo(BufferedSink sink) throws IOException {
            writeTo(file.newWriter(sink));
        }

        public void writeTo(Filer filer) throws IOException {
            writeTo(file.newWriter(filer));
        }

        private void writeTo(Writer writer) throws IOException {
            try {
                // Write each extension
                List<Extension.Processor> list;
                synchronized (extensions) {
                    list = new ArrayList<>(extensions);
                }
                writer.writeList(list);
                writer.flush();
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        }
    }

    public static final class Sourcerer implements Iterable<Extension.Sourcerer> {
        private static final Reader.Parser<Sourcerer> PARSER = new Reader.Parser<Sourcerer>() {
            @Override public Sourcerer parse(Reader reader) throws IOException {
                // Read each extension
                List<Extension.Sourcerer> extensions = Extension.Sourcerer.readList(reader);
                return new Sourcerer(extensions);
            }
        };

        private final ImmutableList<Extension.Sourcerer> extensions;

        private Sourcerer(List<Extension.Sourcerer> extensions) {
            this.extensions = ImmutableList.copyOf(extensions);
        }

        public static Map<Extension, List<MethodSpec>> fromJar(JarInputStream jar) throws IOException {
            MetaInf.File file = INSTANCE.file;
            Map<Extension, List<MethodSpec>> map = new HashMap<>();
            for (MetaInf.Entry<Sourcerer> entry : MetaInf.fromJar(file, jar, PARSER)) {
                addAll(map, entry.value());
            }
            return ImmutableMap.copyOf(map);
        }

        /* visible for testing */
        static Map<Extension, List<MethodSpec>> read(Reader reader) throws IOException {
            Map<Extension, List<MethodSpec>> map = new HashMap<>();
            addAll(map, PARSER.parse(reader));
            return ImmutableMap.copyOf(map);
        }

        private static void addAll(Map<Extension, List<MethodSpec>> map, Sourcerer sourcerers) {
            for (Extension.Sourcerer ext : sourcerers) {
                Extension key = ext.extension();
                List<MethodSpec> values = map.get(key);
                if (values == null) {
                    values = new ArrayList<>();
                    map.put(key, values);
                }
                values.addAll(ext.methods());
            }
        }

        @Override public Iterator<Extension.Sourcerer> iterator() {
            return extensions.iterator();
        }
    }

/*
    private static final class Type extends Extensions {
        private static final String OUTPUT_DIR = "META-INF/sourcerer";
        private static final String FILE_EXTENSION = ".sourcerer";

        private final TypeElement annotationType;
        private final LinkedHashSet<ExtensionClassHelper> extensionClassHelpers = new LinkedHashSet<>();

        private Type(TypeElement annotationType, Extension descriptor) {
            super(descriptor, OUTPUT_DIR, FILE_EXTENSION);
            this.annotationType = annotationType;
        }

        @Override public List<ExtensionClassHelper> extensionClasses() {
            synchronized (extensionClassHelpers) {
                return new ArrayList<>(extensionClassHelpers);
            }
        }

        private void process(TypeElement typeElement) {
            ExtensionClassHelper extensionClassHelper = ExtensionClassHelper.process(descriptor().kind(), typeElement);
            System.out.printf("\nparsed class: %s, extClass: %s\n", typeElement, extensionClassHelper);
            synchronized (extensionClassHelpers) {
                extensionClassHelpers.add(extensionClassHelper);
            }
            System.out.printf("\nthis = %s\n", this);
        }

        @Override
        protected void readExtension(BufferedSource source, TypeSpec.Builder classBuilder) throws IOException {
            ExtensionClassHelper.readMethods(source, this, classBuilder);
        }

        @Override protected void writeExtension(BufferedSink sink, ExtensionClassHelper extensionClassHelper) throws IOException {
            if (descriptor().kind() == ExtensionClass.Kind.StaticDelegate) {
                extensionClassHelper.writeMethods(sink, Modifier.STATIC);
            } else {
                extensionClassHelper.writeMethods(sink);
            }
        }

        @Override public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("annotationType", annotationType)
                    .add("extensionClasses", extensionClasses())
                    .toString();
        }
    }
 */

    //    protected ExtensionType(ExtensionDescriptor descriptor, String outputDir, String fileExtension) {
//        this.descriptor = descriptor;
//        this.outputDir = outputDir;
//        this.fileExtension = fileExtension;
//    }
//
//    public abstract List<? extends ExtensionClassHelper> extensionClasses();
//
//    protected abstract void readExtension(BufferedSource source, TypeSpec.Builder classBuilder) throws IOException;
//
//    protected abstract void writeExtension(BufferedSink sink, ExtensionClassHelper extensionClassHelper) throws IOException;
//
//    public final void read(BufferedSource source, TypeSpec.Builder classBuilder) throws IOException {
//        ExtensionMetadata meta = ExtensionMetadata.from(source);
//        int size = source.readInt();
//        for (int i = 0; i < size; i++) {
//            readExtension(source, classBuilder);
//        }
//    }
//
//    public final void write(Filer filer) throws IOException {
//        final List<? extends ExtensionClassHelper> extensionClasses = new ArrayList<>(extensionClasses());
//        if (extensionClasses.size() == 0) {
//            return;
//        }
//
//        FileObject output = filer.createResource(StandardLocation.CLASS_OUTPUT, "", resourceFilePath());
//        try (BufferedSink sink = Okio.buffer(Okio.sink(output.openOutputStream()))) {
//            // Write method data to buffer
//            writeExtensions(sink, extensionClasses);
//            sink.flush();
//        }
//    }
//
//    private void writeExtensions(BufferedSink sink, List<? extends ExtensionClassHelper> extensions) throws IOException {
//        ExtensionMetadata meta = ExtensionMetadata.create();
//        meta.writeTo(sink);
//
//        int size = extensions.size();
//        sink.writeInt(size);
//        for (int i = 0; i < size; i++) {
//            ExtensionClassHelper extensionClassHelper = extensions.get(i);
//            writeExtension(sink, extensionClassHelper);
//        }
//    }
//
//    public final ExtensionDescriptor descriptor() {
//        return descriptor;
//    }
//
//    public final String resourceFileName() {
//        return descriptor.className() + fileExtension;
//    }
//
//    public final String resourceFilePath() {
//        return outputDir + "/" + resourceFileName();
//    }
//
//    public final String javaPackagePath() {
//        return descriptor.packageName().replace('.', '/');
//    }
//
//    public final String javaFileName() {
//        return descriptor.className() + ".java";
//    }
//
//    public final SourceWriter sourceWriter() {
//        return new SourceWriter(this);
//    }
}
