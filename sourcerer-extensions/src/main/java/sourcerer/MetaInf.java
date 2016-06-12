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
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import sourcerer.exceptions.IncompatibleVersionException;
import sourcerer.exceptions.UnknownHeaderException;
import sourcerer.io.Descriptor;
import sourcerer.io.Reader;
import sourcerer.io.Value64;
import sourcerer.io.Writer;

public class MetaInf extends Descriptor {
    private static final String DIR = "META-INF/sourcerer";
    private static final String FILE_EXTENSION = ".srcr";

    protected MetaInf(String dir) {
        super(dir(dir), FILE_EXTENSION);
    }

    public static MetaInf create(String dir) {
        return new MetaInf(dir);
    }

    public static MetaInf.File file(String dir, String fileName) {
        return new MetaInf(dir)
                .file(fileName);
    }

    public static <T> ListMultimap<MetaInf, Entry<T>> fromJar(List<MetaInf> metaList, JarInputStream jar,
            Reader.Parser<T> parser) throws IOException {
        JarEntry jarEntry;
        ImmutableListMultimap.Builder<MetaInf, Entry<T>> map = ImmutableListMultimap.builder();
        while ((jarEntry = jar.getNextJarEntry()) != null) {
            for (MetaInf metaInf : metaList) {
                Entry<T> entry = metaInf.entry(jar, jarEntry, parser);
                if (entry != null) {
                    map.put(metaInf, entry);
                }
            }
        }
        return map.build();
    }

    public static <T> List<Entry<T>> fromJar(MetaInf.File metaFile, JarInputStream jar,
            Reader.Parser<T> parser) throws IOException {
        MetaInf metaInf = metaFile.descriptor();
        JarEntry jarEntry;
        ImmutableList.Builder<Entry<T>> list = ImmutableList.builder();
        while ((jarEntry = jar.getNextJarEntry()) != null) {
            if (metaFile.matches(jarEntry)) {
                list.add(metaInf.entry(jar, metaFile, parser));
            }
        }
        return list.build();
    }

    public static <T> List<Entry<T>> fromJar(MetaInf metaInf, JarInputStream jar,
            Reader.Parser<T> parser) throws IOException {
        JarEntry jarEntry;
        ImmutableList.Builder<Entry<T>> list = ImmutableList.builder();
        while ((jarEntry = jar.getNextJarEntry()) != null) {
            Entry<T> entry = metaInf.entry(jar, jarEntry, parser);
            if (entry != null) {
                list.add(entry);
            }
        }
        return list.build();
    }

    private static String dir(String dir) {
        if (dir.isEmpty()) throw new IllegalArgumentException("dir must not be empty");
        return DIR + "/" + dir;
    }

    public File parseFile(JarEntry entry) {
        final String path = entry.getName();
        final String dir = dir();
        if (path.startsWith(dir)) {
            String other = path.substring(dir.length(), path.length());
            if (other.length() >= 2 && other.startsWith("/")) {
                return file(other.substring(1, other.length()));
            }
        }
        return null;
    }

    @Override public File file(String fileName) {
        return new File(fileName);
    }

    private <T> Entry<T> entry(JarInputStream jar, JarEntry jarEntry, Reader.Parser<T> parser) throws IOException {
        MetaInf.File metaFile = parseFile(jarEntry);
        if (metaFile != null) {
            return entry(jar, metaFile, parser);
        }
        return null;
    }

    private <T> Entry<T> entry(JarInputStream jar, MetaInf.File metaFile, Reader.Parser<T> parser) throws IOException {
        Reader reader = Reader.newReader(jar);
        metaFile.assertCanRead(reader);
        T value = parser.parse(reader);
        return new Entry<>(metaFile, value);
    }

    private interface FileConstants {
        Value64 HEADER = Value64.from("sourcerer");
        Value64 VERSION = Value64.from(1);
    }

    public class File extends Descriptor.File implements FileConstants {
        final String FORMAT = "Cannot read from the source. '%s' should be '%s', but is '%s'";

        protected File(File file) {
            super(file);
        }

        protected File(String fileName) {
            super(fileName);
        }

        @Override public final void writeTo(Writer writer) throws IOException {
            writer.write(HEADER);
            writer.write(VERSION);
        }

        public final void assertCanRead(Reader reader) throws IOException {
            Value64 header = Value64.read(reader);
            if (!HEADER.equals(header)) {
                String msg = String.format(Locale.US, FORMAT, "Header", HEADER, header);
                throw new UnknownHeaderException(msg);
            }
            Value64 version = Value64.read(reader);
            if (VERSION.equals(version)) {
                String msg = String.format(Locale.US, FORMAT, "Version", VERSION, version);
                throw new IncompatibleVersionException(msg);
            }
        }

        @Override public MetaInf descriptor() {
            return MetaInf.this;
        }
    }

    public static class Entry<T> {
        private final MetaInf.File metaFile;
        private final T value;

        protected Entry(MetaInf.File metaFile, T value) {
            this.metaFile = metaFile;
            this.value = value;
        }

        public final MetaInf.File metaFile() {
            return metaFile;
        }

        public final T value() {
            return value;
        }
    }
}
