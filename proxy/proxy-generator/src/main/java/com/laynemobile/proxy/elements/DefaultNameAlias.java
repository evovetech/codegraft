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

package com.laynemobile.proxy.elements;

import com.google.common.base.Objects;

import javax.lang.model.element.Name;

import static java.util.Locale.US;

final class DefaultNameAlias implements NameAlias {
    private final char[] chars;
    private final int offset;
    private final int length;
    private final String toString;

    private DefaultNameAlias(char[] chars) {
        this(chars, 0, chars.length);
    }

    private DefaultNameAlias(char[] chars, int offset, int length) {
        if (offset >= length) {
            String msg = String.format(US, "offset '%d' is greater than length '%d'", offset, length);
            throw new IllegalArgumentException(msg);
        } else if (offset + length > chars.length) {
            String msg = String.format(US, "offset '%d' + length '%d' is greater than char array length '%d'",
                    offset, length, chars.length);
            throw new IllegalArgumentException(msg);
        }
        this.offset = offset;
        this.length = length;
        this.chars = chars;
        this.toString = String.valueOf(chars, offset, length);
    }

    static NameAlias of(Name name) {
        if (name instanceof NameAlias) {
            return (NameAlias) name;
        }
        final int length = name.length();
        final char[] chars = new char[length];
        for (int i = 0; i < length; i++) {
            chars[i] = name.charAt(i);
        }
        return new DefaultNameAlias(chars);
    }

    @Override public int length() {
        return length;
    }

    @Override public char charAt(int index) {
        return chars[offset + index];
    }

    @Override public CharSequence subSequence(int start, int end) {
        int _offset = offset + start;
        int _length = end - start;
        if (_offset + _length > length) {
            String msg = String.format(US, "offset '%d' + length '%d' is greater than char array length '%d'",
                    _offset, _length, length);
            throw new IllegalArgumentException(msg);
        }
        return new StringBuffer(_length)
                .append(chars, _offset, _length);
    }

    @Override public boolean contentEquals(CharSequence cs) {
        if (cs == null) return false;

        final int len = length;
        if (len != cs.length()) return false;
        for (int i = 0; i < len; i++) {
            if (charAt(i) != cs.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    @Override public boolean equals(Object o) {
        return this == o ||
                o instanceof DefaultNameAlias && contentEquals((DefaultNameAlias) o);
    }

    @Override public int hashCode() {
        return Objects.hashCode(toString);
    }

    @Override public String toString() {
        return toString;
    }
}
