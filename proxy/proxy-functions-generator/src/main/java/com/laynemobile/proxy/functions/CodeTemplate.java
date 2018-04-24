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

import com.google.common.base.Joiner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import static java.util.Locale.US;

public class CodeTemplate {
    private static final String TOKEN_FORMAT = "${%s}";

    private final String template;
    private String output;

    public CodeTemplate(String template) {
        this.template = template;
        this.output = template;
    }

    private static final String token(String keyword) {
        return String.format(US, TOKEN_FORMAT, keyword);
    }

    public final String output() {
        return output;
    }

    public CodeTemplate reset() {
        output = template;
        return this;
    }

    public final CodeTemplate fill(String keyword, String value) {
        String token = token(keyword);
        output = output.replace(token, value);
        return this;
    }

    public final CodeTemplate fill(String keyword, Object value) {
        return value == null
                ? fill(keyword, "null")
                : fill(keyword, value.toString());
    }

    public final CodeTemplate fill(String keyword, String[] values) {
        String value = Joiner.on(", ")
                .join(values);
        return fill(keyword, value);
    }

    public final void writeTo(Writer writer) throws IOException {
        writer.write(output);
    }

    public void writeTo(File file) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        try {
            writer.flush();
            writeTo(writer);
        } finally {
            writer.close();
        }
    }
}
