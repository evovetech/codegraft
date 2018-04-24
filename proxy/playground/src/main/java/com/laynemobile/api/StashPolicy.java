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

package com.laynemobile.api;

public enum StashPolicy {
    STASH_ONLY_NO_SOURCE(0),
    STASH_UNLESS_EXPIRED(1),
    STASH_THEN_SOURCE(2),
    STASH_THEN_SOURCE_IF_EXPIRED(3),
    SOURCE(4),
    SOURCE_UNLESS_ERROR(5),
    SOURCE_ONLY_NO_STASH(6);

    public static final StashPolicy DEFAULT = STASH_THEN_SOURCE_IF_EXPIRED;

    private final int id;

    StashPolicy(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
