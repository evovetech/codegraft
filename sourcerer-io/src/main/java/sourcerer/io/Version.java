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

import okio.ByteString;

public final class Version implements Writeable, Comparable<Integer> {
    private final Integer value;

    private Version(int value) {
        this.value = value;
    }

    public static Version version(int value) {
        return new Version(value);
    }

    public static Version readVersion(Reader reader) throws IOException {
        ByteString byteString = reader.read();

        return reader.readVersion();
    }

    public int value() {
        return value;
    }

    @Override public void writeTo(Writer writer) throws IOException {
        writer.writeVersion(this);
    }

    @Override public int compareTo(Integer o) {
        return value.compareTo(o);
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Version)) return false;
        Version that = (Version) o;
        return value == that.value;
    }

    @Override public int hashCode() {
        return Objects.hashCode(value);
    }
}
