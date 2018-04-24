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

package com.laynemobile.proxy.functions;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import javax.annotation.processing.Filer;
import javax.tools.JavaFileObject;

public class FileTemplate extends CodeTemplate {
    final String packageName;
    final String className;

    public FileTemplate(String packageName, String className, String template) {
        super(template);
        this.packageName = packageName;
        this.className = className;
        reset();
    }

    @Override public FileTemplate reset() {
        super.reset()
                .fill("PACKAGE_NAME", packageName)
                .fill("CLASS_NAME", className);
        return this;
    }

    @Override public final void writeTo(File dir) throws IOException {
        dir.mkdirs();
        super.writeTo(new File(dir, className + ".java"));
    }

    public final void writeTo(Filer filer) throws IOException {
        String fileName = packageName.isEmpty()
                ? className
                : packageName + "." + className;
        JavaFileObject filerSourceFile = filer.createSourceFile(fileName);
        try (Writer writer = filerSourceFile.openWriter()) {
            writeTo(writer);
        } catch (Exception e) {
            try {
                filerSourceFile.delete();
            } catch (Exception ignored) {
            }
            throw e;
        }
    }
}
