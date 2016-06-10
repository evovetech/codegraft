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
import java.util.EnumMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import okio.BufferedSource;
import okio.Okio;

public final class MetaFile {
    private static final String ROOT_DIR = "META-INF/sourcerer";
    private static final String EXTENSION_DIR = ROOT_DIR + "/extension";
    private static final String EXTENSION_PARENT_DIR = EXTENSION_DIR + "/parent";
    private static final String FILE_EXTENSION = ".srcr";

    private final Type type;
    private final Descriptor.File file;

    private MetaFile(Type type, Descriptor.File file) {
        this.type = type;
        this.file = file;
    }

    public Type type() {
        return type;
    }

    public Descriptor.File file() {
        return file;
    }

    public enum Type {
        ExtensionParent(new Descriptor(EXTENSION_PARENT_DIR, FILE_EXTENSION)),
        Extension(new Descriptor(EXTENSION_DIR, FILE_EXTENSION));

        private final Descriptor descriptor;

        Type(Descriptor descriptor) {
            this.descriptor = descriptor;
        }

        public static EnumMap<Type, List<MetaFile.Reader>> parse(ZipInputStream zis) throws IOException {
            EnumMap<Type, List<MetaFile.Reader>> map = new EnumMap<>(Type.class);
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                for (Type type : values()) {
                    MetaFile.Reader reader = type.parseReader(zis, entry);
                    if (reader != null) {
                        List<MetaFile.Reader> readers = map.get(type);
                        if (readers == null) {
                            readers = new ArrayList<>();
                            map.put(type, readers);
                        }
                        readers.add(reader);
                    }
                }
            }
            return map;
        }

        public Descriptor descriptor() {
            return descriptor;
        }

        private MetaFile parseMetaFile(ZipEntry zipEntry) {
            Descriptor.File file = descriptor.parseFile(zipEntry);
            if (file != null) {
                return new MetaFile(this, file);
            }
            return null;
        }

        private MetaFile.Reader parseReader(ZipInputStream zis, ZipEntry zipEntry) {
            MetaFile file = parseMetaFile(zipEntry);
            if (file != null) {
                BufferedSource source = Okio.buffer(Okio.source(zis));
                return new Reader(file, source);
            }
            return null;
        }
    }

    public static class Reader extends sourcerer.Reader {
        private final MetaFile metaFile;

        private Reader(MetaFile metaFile, BufferedSource source) {
            super(metaFile.file, source);
            this.metaFile = metaFile;
        }

        public MetaFile metaFile() {
            return metaFile;
        }
    }
}
