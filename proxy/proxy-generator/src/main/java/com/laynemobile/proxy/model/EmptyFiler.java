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

package com.laynemobile.proxy.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;

public class EmptyFiler implements Filer {
    private final Filer actual;

    private EmptyFiler(Filer actual) {
        this.actual = actual;
    }

    public static EmptyFiler wrap(Filer filer) {
        if (filer instanceof EmptyFiler) {
            return (EmptyFiler) filer;
        }
        return new EmptyFiler(filer);
    }

    @Override public JavaFileObject createSourceFile(CharSequence name, Element... originatingElements)
            throws IOException {
        return actual.createSourceFile(name, originatingElements);
    }

    @Override public JavaFileObject createClassFile(CharSequence name, Element... originatingElements)
            throws IOException {
        return actual.createClassFile(name, originatingElements);
    }

    @Override
    public FileObject createResource(JavaFileManager.Location location, CharSequence pkg, CharSequence relativeName,
            Element... originatingElements) throws IOException {
        return actual.createResource(location, pkg, relativeName, originatingElements);
    }

    @Override
    public FileObject getResource(JavaFileManager.Location location, CharSequence pkg, CharSequence relativeName)
            throws IOException {
        return actual.getResource(location, pkg, relativeName);
    }

    private static class EmptyFileObject implements FileObject {
        private final FileObject actual;

        private EmptyFileObject(FileObject actual) {
            this.actual = actual;
        }

        @Override public URI toUri() {
            return actual.toUri();
        }

        @Override public String getName() {
            return actual.getName();
        }

        @Override public InputStream openInputStream() throws IOException {
            return actual.openInputStream();
        }

        @Override public OutputStream openOutputStream() throws IOException {
            return actual.openOutputStream();
        }

        @Override public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
            return actual.openReader(ignoreEncodingErrors);
        }

        @Override public CharSequence getCharContent(boolean ignoreEncodingErrors)
                throws IOException {
            return actual.getCharContent(ignoreEncodingErrors);
        }

        @Override public Writer openWriter() throws IOException {
            return actual.openWriter();
        }

        @Override public long getLastModified() {
            return actual.getLastModified();
        }

        @Override public boolean delete() {
            return actual.delete();
        }
    }
}
