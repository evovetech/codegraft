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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import javax.annotation.processing.Filer;

import sourcerer.io.Writer;

public final class Extensions {
    private static final String DIR = "ext";
    private static final String FILE_NAME = "extensions";

    private final MetaInf.File file;

    public Extensions() {
        this.file = MetaInf.file(DIR, FILE_NAME);
    }

    public static Processor processor() {
        return new Extensions()
                .newProcessor();
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

        public void writeTo(Filer filer) throws IOException {
            Writer writer = file.newWriter(filer);

            // HEADER, VERSION

            // Each extension
            List<Extension.Processor> list;
            synchronized (extensions) {
                list = new ArrayList<>(extensions);
            }
            writer.writeList(list);
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
            ExtensionClassHelper extensionClassHelper = ExtensionClassHelper.parse(descriptor().kind(), typeElement);
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
