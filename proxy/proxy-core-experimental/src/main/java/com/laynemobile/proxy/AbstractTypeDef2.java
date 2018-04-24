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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static java.util.Collections.unmodifiableSortedSet;

abstract class AbstractTypeDef2<T, S extends BaseTypeDef2<? super T, ? extends S, ? extends F>, F> implements BaseTypeDef2<T, S, F> {
    private final TypeToken<T> type;
    private final SortedSet<? extends S> superTypes;

    AbstractTypeDef2(TypeToken<T> type, Collection<? extends S> superTypes) {
        this.type = type;
        this.superTypes = unmodifiableSortedSet(new TreeSet<>(superTypes));
    }

    @Override public final TypeToken<T> type() {
        return type;
    }

    @Override public SortedSet<? extends S> superTypes() {
        return superTypes;
    }

    @Override public final Set<Class<?>> rawTypes() {
        SortedSet<? extends S> superTypes = superTypes();
        LinkedHashSet<Class<?>> rawTypes = new LinkedHashSet<>(superTypes.size() + 1);
        rawTypes.add(type().getRawType());
        for (S superType : superTypes) {
            rawTypes.add(superType.type().getRawType());
        }
        return rawTypes;
    }

    @Override public abstract List<? extends F> functions();

    @Override public final Set<? extends F> allFunctions() {
        LinkedHashSet<F> allFunctions = new LinkedHashSet<>(functions());
        for (S superType : superTypes) {
            for (F function : superType.allFunctions()) {
                if (!allFunctions.contains(function)) {
                    allFunctions.add(function);
                }
            }
        }
        return allFunctions;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TypeDef)) return false;
        TypeDef<?> that = (TypeDef<?>) o;
        return Objects.equal(type, that.type()) &&
                Objects.equal(superTypes, that.superTypes());
    }

    @Override public int hashCode() {
        return Objects.hashCode(type, superTypes);
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("type", type)
                .toString();
    }

    @Override public int compareTo(BaseTypeDef2<?, ?, ?> o) {
        if (equals(o)) {
            return 0;
        } else if (dependsOn(o, this)) {
            return -1;
        } else if (dependsOn(this, o)) {
            return 1;
        }
        return name(this).compareTo(name(o));
    }

    private static boolean dependsOn(BaseTypeDef2<?, ?, ?> type, BaseTypeDef2<?, ?, ?> test) {
        for (BaseTypeDef2<?, ?, ?> superType : type.superTypes()) {
            if (superType.equals(test)) {
                return true;
            } else if (dependsOn(superType, test)) {
                return true;
            }
        }
        return false;
    }

    private static String name(BaseTypeDef2<?, ?, ?> o) {
        return o.type().getRawType().getSimpleName();
    }
}
