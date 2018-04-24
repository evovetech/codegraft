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

package com.laynemobile.proxy;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public abstract class AbstractValueAlias<T> implements ValueAlias<T> {
    private final T value;

    protected AbstractValueAlias(AbstractValueAlias<? extends T> alias) {
        this.value = alias.value;
    }

    protected AbstractValueAlias(T value) {
        this.value = value;
    }

    @Override public final T value() {
        return value;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractValueAlias)) return false;
        AbstractValueAlias<?> that = (AbstractValueAlias<?>) o;
        return Objects.equal(value, that.value);
    }

    @Override public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("value", value)
                .toString();
    }

    @Override public String toDebugString() {
        return MoreObjects.toStringHelper(this)
                .add("value", value)
                .toString();
    }
}
