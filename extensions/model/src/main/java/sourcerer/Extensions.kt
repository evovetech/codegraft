/*
 * Copyright 2018 evove.tech
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

package sourcerer

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.squareup.javapoet.MethodSpec
import okio.BufferedSink
import sourcerer.io.Reader
import sourcerer.io.Writer
import java.io.IOException
import java.util.ArrayList
import java.util.HashMap
import java.util.LinkedHashSet
import java.util.jar.JarInputStream
import javax.annotation.processing.Filer

class Extensions private constructor() {

    private val file: MetaInf.File

    init {
        this.file = MetaInf.file(DIR, FILE_NAME)
    }

    fun file(): MetaInf.File {
        return file
    }

    fun newProcessor(): Processor {
        return Processor()
    }

    class Sourcerer private constructor(extensions: List<Extension.Sourcerer>) : Iterable<Extension.Sourcerer> {

        private
        val extensions: ImmutableList<Extension.Sourcerer> =
            ImmutableList.copyOf(extensions)

        override
        fun iterator(): Iterator<Extension.Sourcerer> {
            return extensions.iterator()
        }

        companion object {
            private val PARSER = object : Reader.Parser<Sourcerer> {
                @Throws(IOException::class)
                override fun parse(reader: Reader): Sourcerer {
                    // Read each extension
                    val extensions = Extension.Sourcerer.readList(reader)
                    return Sourcerer(extensions)
                }
            }

            @Throws(IOException::class)
            fun fromJar(jar: JarInputStream): Map<Extension, MutableList<MethodSpec>> {
                val file = INSTANCE.file
                val map = HashMap<Extension, MutableList<MethodSpec>>()
                for (entry in MetaInf.fromJar(file, jar, PARSER)) {
                    addAll(map, entry.value)
                }
                return ImmutableMap.copyOf(map)
            }

            /* visible for testing */
            @Throws(IOException::class)
            internal fun read(reader: Reader): Map<Extension, MutableList<MethodSpec>> {
                val map = HashMap<Extension, MutableList<MethodSpec>>()
                addAll(map, PARSER.parse(reader))
                return ImmutableMap.copyOf(map)
            }

            private fun addAll(
                map: MutableMap<Extension, MutableList<MethodSpec>>,
                sourcerers: Sourcerer
            ) {
                for (ext in sourcerers) {
                    val key = ext.extension()
                    var values: MutableList<MethodSpec>? = map[key]
                    if (values == null) {
                        values = ArrayList()
                        map[key] = values
                    }
                    values.addAll(ext.methods())
                }
            }
        }
    }

    inner
    class Processor {
        private
        val extensions: LinkedHashSet<Extension.Processor> =
            LinkedHashSet()

        fun extensions(): Extensions {
            return this@Extensions
        }

        fun add(extensionClass: ExtensionClass): Extension.Processor {
            val ext = Extension.create(extensionClass)
            synchronized(extensions) {
                for (next in extensions) {
                    if (ext == next.extension()) {
                        return next
                    }
                }
                val processor = ext.newProcessor()
                extensions.add(processor)
                return processor
            }
        }

        /* visible for testing */
        @Throws(IOException::class)
        fun writeTo(sink: BufferedSink) = file.newWriter(sink)
                .write()

        @Throws(IOException::class)
        fun writeTo(filer: Filer) = file.newWriter(filer)
                .write()

        private
        fun Writer.write() = use { writer ->
            // Write each extension
            val list: List<Extension.Processor> = synchronized(extensions) {
                ArrayList(extensions)
            }
            writer.writeList(list)
            writer.flush()
        }
    }

    companion object {
        private val DIR = "ext"
        private val FILE_NAME = "extensions"

        private val INSTANCE = Extensions()

        fun processor(): Processor {
            return INSTANCE.newProcessor()
        }

        fun instance(): Extensions {
            return INSTANCE
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
