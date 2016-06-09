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

import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import okio.BufferedSink;
import okio.BufferedSource;
import okio.ByteString;
import okio.Okio;

import static sourcerer.Constants.FILE_HEADER;
import static sourcerer.Constants.FILE_VERSION;

public abstract class ExtensionType {
    private final ExtensionDescriptor descriptor;
    private final String outputDir;
    private final String fileExtension;

    protected ExtensionType(ExtensionDescriptor descriptor, String outputDir, String fileExtension) {
        this.descriptor = descriptor;
        this.outputDir = outputDir;
        this.fileExtension = fileExtension;
    }

    public abstract List<? extends ExtensionClass> extensionClasses();

    protected abstract void readExtension(BufferedSource source, TypeSpec.Builder classBuilder) throws IOException;

    protected abstract void writeExtension(BufferedSink sink, ExtensionClass extensionClass) throws IOException;

    public final void read(BufferedSource source, TypeSpec.Builder classBuilder) throws IOException {
        ByteString header = source.readByteString(FILE_HEADER.size());
        if (!FILE_HEADER.equals(header)) {
            throw new IOException("Cannot read from the source. Header is not set");
        } else if (source.readInt() != FILE_VERSION) {
            throw new IOException("Cannot read from the source. Version is not current");
        }
        int size = source.readInt();
        for (int i = 0; i < size; i++) {
            readExtension(source, classBuilder);
        }
    }

    public final void write(Filer filer) throws IOException {
        final List<? extends ExtensionClass> extensionClasses = new ArrayList<>(extensionClasses());
        if (extensionClasses.size() == 0) {
            return;
        }

        FileObject output = filer.createResource(StandardLocation.CLASS_OUTPUT, "", resourceFilePath());
        try (BufferedSink sink = Okio.buffer(Okio.sink(output.openOutputStream()))) {
            // Write method data to buffer
            writeExtensions(sink, extensionClasses);
            sink.flush();
        }
    }

    private void writeExtensions(BufferedSink sink, List<? extends ExtensionClass> extensions) throws IOException {
        sink.write(FILE_HEADER);
        sink.writeInt(FILE_VERSION);

        int size = extensions.size();
        sink.writeInt(size);
        for (int i = 0; i < size; i++) {
            ExtensionClass extensionClass = extensions.get(i);
            writeExtension(sink, extensionClass);
        }
    }

    public final ExtensionDescriptor descriptor() {
        return descriptor;
    }

    public final String resourceFileName() {
        return descriptor.className() + fileExtension;
    }

    public final String resourceFilePath() {
        return outputDir + "/" + resourceFileName();
    }

    public final String javaPackagePath() {
        return descriptor.packageName().replace('.', '/');
    }

    public final String javaFileName() {
        return descriptor.className() + ".java";
    }

    public final SourceWriter sourceWriter() {
        return new SourceWriter(this);
    }
}
