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

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;

import java.io.IOException;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.zip.ZipInputStream;

import javax.annotation.processing.Filer;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import sourcerer.io.Descriptor;
import sourcerer.io.Reader;
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

    private <T> Entry<T> entry(ZipInputStream zis, JarEntry jarEntry, Reader.Parser<T> parser) throws IOException {
        MetaInf.File metaFile = parseFile(jarEntry);
        if (metaFile != null) {
            T value = parser.parse(Reader.newReader(zis));
            return new Entry<>(metaFile, value);
        }
        return null;
    }

    public class File extends Descriptor.File {
        protected File(File file) {
            super(file);
        }

        protected File(String fileName) {
            super(fileName);
        }

        public Writer newWriter(Filer filer) throws IOException {
            FileObject output = filer.createResource(StandardLocation.CLASS_OUTPUT, "", extFilePath());
            return Writer.newWriter(output.openOutputStream());
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
