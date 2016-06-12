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

package sourcerer.io;

import com.google.common.base.Objects;

import java.io.IOException;
import java.util.List;

import okio.ByteString;

public final class Value64 implements Writeable {
    private static final Reader.Parser<Value64> PARSER = new Reader.Parser<Value64>() {
        @Override public final Value64 parse(Reader reader) throws IOException {
            String base64 = reader.readString();
            String value = ByteString.decodeBase64(base64)
                    .utf8();
            return new Value64(value, base64);
        }
    };

    private final String value;
    private final String base64;

    private Value64(String value) {
        this(value, ByteString.encodeUtf8(value).base64());
    }

    private Value64(String value, String base64) {
        this.value = value;
        this.base64 = base64;
    }

    public static Value64 from(int value) {
        return new Value64(Integer.toString(value));
    }

    public static Value64 from(String value) {
        return new Value64(value);
    }

    public static Value64 read(Reader reader) throws IOException {
        return PARSER.parse(reader);
    }

    public static List<Value64> readList(Reader reader) throws IOException {
        return reader.readList(PARSER);
    }

    public String value() {
        return value;
    }

    public int intValue() throws NumberFormatException {
        return Integer.parseInt(value);
    }

    @Override public void writeTo(Writer writer) throws IOException {
        writer.writeString(base64);
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Value64)) return false;
        Value64 value64 = (Value64) o;
        return Objects.equal(value, value64.value);
    }

    @Override public int hashCode() {
        return Objects.hashCode(value);
    }
}
