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

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

interface BaseTypeDef2<T, S extends BaseTypeDef2<? super T, ? extends S, ? extends F>, F> extends Comparable<BaseTypeDef2<?, ?, ?>> {
    TypeToken<T> type();

    SortedSet<? extends S> superTypes();

    Set<Class<?>> rawTypes();

    List<? extends F> functions();

    Set<? extends F> allFunctions();

    ProxyType2.Builder<T> newProxyBuilder();
}
