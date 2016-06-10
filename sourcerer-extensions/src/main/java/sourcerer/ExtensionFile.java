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

import com.google.common.base.Objects;

import java.io.IOException;

import okio.BufferedSink;
import okio.BufferedSource;
import okio.ByteString;
import sourcerer.exceptions.IncompatibleVersionException;
import sourcerer.exceptions.UnknownHeaderException;

public final class ExtensionFile implements Constants {
    private final Type type;
    private final MetaFile descriptor;
    private final Metadata metadata;

    private ExtensionFile(Type type) {
        this(type, new Metadata());
    }

    public ExtensionFile(Type type, Metadata metadata) {
        this.type = type;
        this.descriptor = type.descriptor();
        this.metadata = metadata;
    }

    private static ExtensionFile create(Type type) {
        return new ExtensionFile(type);
    }

    private static ExtensionFile from(BufferedSource source) throws IOException {
        ByteString header;
        int version;
        if (!HEADER.equals((header = source.readByteString(HEADER.size())))) {
            throw new UnknownHeaderException("Cannot read from the source. Header is not set");
        } else if ((version = source.readInt()) != VERSION) {
            throw new IncompatibleVersionException("Cannot read from the source. Version is not current");
        }
        return new ExtensionFile(header, version);
    }

    public Metadata metadata() {
        return metadata;
    }

    public static final class Metadata {
        private final ByteString header;
        private final int version;

        private Metadata() {
            this(HEADER, VERSION);
        }

        private Metadata(ByteString header, int version) {
            this.header = header;
            this.version = version;
        }

        private void writeTo(BufferedSink sink) throws IOException {
            sink.write(header);
            sink.writeInt(version);
        }

        public ByteString header() {
            return header;
        }

        public int version() {
            return version;
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Metadata)) return false;
            Metadata that = (Metadata) o;
            return version == that.version &&
                    Objects.equal(header, that.header);
        }

        @Override public int hashCode() {
            return Objects.hashCode(header, version);
        }
    }

    public enum Type {
        ExtensionDescriptor(new MetaFile.Descriptor("descriptor", ".srcrdesc")),
        ExtensionSource(new MetaFile.Descriptor("source", ".srcrext"));

        private final MetaFile.Descriptor descriptor;

        Type(MetaFile.Descriptor descriptor) {
            this.descriptor = descriptor;
        }

        public MetaFile.Descriptor descriptor() {
            return descriptor;
        }
    }

//    public final void read(BufferedSource source, TypeSpec.Builder classBuilder) throws IOException {
//        ExtensionMetadata meta = ExtensionMetadata.from(source);
//        int size = source.readInt();
//        for (int i = 0; i < size; i++) {
//            readExtension(source, classBuilder);
//        }
//    }
//
//    public final void write(Filer filer) throws IOException {
//        final List<? extends ExtensionClass> extensionClasses = new ArrayList<>(extensionClasses());
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
//    private void writeExtensions(BufferedSink sink, List<? extends ExtensionClass> extensions) throws IOException {
//        ExtensionMetadata meta = ExtensionMetadata.create();
//        meta.writeTo(sink);
//
//        int size = extensions.size();
//        sink.writeInt(size);
//        for (int i = 0; i < size; i++) {
//            ExtensionClass extensionClass = extensions.get(i);
//            writeExtension(sink, extensionClass);
//        }
//    }

//    private final ByteString header;
//    private final int version;
//
//    private ExtensionFile(ByteString header, int version) {
//        this.header = header;
//        this.version = version;
//    }

//
//    static String readString(BufferedSource source) throws IOException {
//        int length = source.readInt();
//        return source.readUtf8(length);
//    }

//    public final void read(BufferedSource source, TypeSpec.Builder classBuilder) throws IOException {
//        ByteString header = source.readByteString(HEADER.size());
//        if (!HEADER.equals(header)) {
//            throw new IOException("Cannot read from the source. Header is not set");
//        } else if (source.readInt() != VERSION) {
//            throw new IOException("Cannot read from the source. Version is not current");
//        }
//
//        int size = source.readInt();
//        for (int i = 0; i < size; i++) {
//            readExtension(source, classBuilder);
//        }
//    }
//
//    public final void write(Filer filer) throws IOException {
//        final List<? extends ExtensionClass> extensionClasses = new ArrayList<>(extensionClasses());
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

    private static abstract class Handler {
        private final ExtensionFile file;

        protected Handler(ExtensionFile file) {
            this.file = file;
        }
    }

    public static final class Reader extends Handler {
        private final BufferedSource source;

        private Reader(ExtensionFile file, BufferedSource source) {
            super(file);
            this.source = source;
        }

        public static Reader from(BufferedSource source) throws IOException {
            return new Reader(ExtensionFile.from(source), source);
        }

        public String readUtf8Entry() throws IOException {
            int length = source.readInt();
            return source.readUtf8(length);
        }
    }

    public static final class Writer extends Handler {
        private final BufferedSink sink;

        private Writer(ExtensionFile file, BufferedSink sink) {
            super(file);
            this.sink = sink;
        }

        public static Writer from(BufferedSink sink) throws IOException {
            ExtensionFile file = ExtensionFile.create();
            file.writeTo(sink);
            return new Writer(file, sink);
        }

        public Writer writeUtf8Entry(String entry) throws IOException {
            ByteString val = ByteString.encodeUtf8(entry);
            sink.writeInt(val.size());
            sink.write(val);
            return this;
        }
    }
}
