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
import com.squareup.javapoet.MethodSpec;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarInputStream;

import javax.annotation.processing.Filer;

import okio.Buffer;
import sourcerer.io.Reader;

public class SourceWriters {
    private final Map<Extension, List<MethodSpec>> extensions = new HashMap<>();

    public void read(InputStream is) throws IOException {
        try (JarInputStream jar = new JarInputStream(new BufferedInputStream(is))) {
            addAll(Extensions.Sourcerer.fromJar(jar));
        }
    }

    /* visible for testing */
    void read(Buffer source) throws IOException {
        Reader reader = Reader.newReader(source);
        MetaInf.File metaFile = Extensions.instance().file();
        metaFile.assertCanRead(reader);
        addAll(Extensions.Sourcerer.read(reader));
    }

    private void addAll(Map<Extension, List<MethodSpec>> map) {
        synchronized (extensions) {
            for (Map.Entry<Extension, List<MethodSpec>> entry : map.entrySet()) {
                Extension ext = entry.getKey();
                List<MethodSpec> methods = extensions.get(ext);
                if (methods == null) {
                    methods = new ArrayList<>();
                    extensions.put(ext, methods);
                }
                methods.addAll(entry.getValue());
            }
        }
    }

    void writeTo(Filer filer) throws IOException {
        for (SourceWriter sourceWriter : sourceWriters()) {
            sourceWriter.writeTo(filer);
        }
    }

    public void writeTo(File outputDir) throws IOException {
        for (SourceWriter sourceWriter : sourceWriters()) {
            sourceWriter.writeTo(outputDir);
        }
    }

    private List<SourceWriter> sourceWriters() {
        ImmutableList.Builder<SourceWriter> sourceWriters = ImmutableList.builder();
        synchronized (extensions) {
            for (Map.Entry<Extension, List<MethodSpec>> entry : extensions.entrySet()) {
                Extension.Sourcerer sourcerer = Extension.Sourcerer.create(entry.getKey(), entry.getValue());
                sourceWriters.add(sourcerer.newSourceWriter());
            }
        }
        return sourceWriters.build();
    }
}
