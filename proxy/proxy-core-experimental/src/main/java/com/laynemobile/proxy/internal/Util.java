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

package com.laynemobile.proxy.internal;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class Util {
    private static final String TAG = Util.class.getSimpleName();

    private Util() { throw new AssertionError("no instances"); }

    public static <T> Collection<T> nullSafe(Collection<T> collection) {
        return collection == null ? Collections.<T>emptyList() : collection;
    }

    public static <T> List<T> nullSafe(List<T> list) {
        return list == null ? Collections.<T>emptyList() : list;
    }

    public static <T> Set<T> nullSafe(Set<T> set) {
        return set == null ? Collections.<T>emptySet() : set;
    }

    @SafeVarargs public static <T> List<T> list(T... t) {
        return t == null
                ? Collections.<T>emptyList()
                : Arrays.asList(t);
    }

    public static void close(Object object) throws IOException {
        if (object instanceof Closeable) {
            ((Closeable) object).close();
        }
    }

    public static void closeQuietly(Object object) {
        try {
            close(object);
        } catch (IOException e) {
            ProxyLog.e(TAG, "Error closing", e);
        }
    }

    public static <T> T checkNotNull(T obj) {
        if (obj == null) {
            throw new NullPointerException();
        }
        return obj;
    }

    public static void checkArgument(boolean condition) {
        if (!condition) {
            throw new IllegalArgumentException();
        }
    }
}
