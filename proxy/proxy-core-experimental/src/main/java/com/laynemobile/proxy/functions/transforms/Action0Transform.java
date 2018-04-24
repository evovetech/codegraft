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

package com.laynemobile.proxy.functions.transforms;

import com.laynemobile.proxy.functions.Action0;
import com.laynemobile.proxy.functions.Actions;

public class Action0Transform
        extends ActionTransform<Action0>
        implements Action0 {
    private static final Action0Transform EMPTY = new Action0Transform();

    public Action0Transform() {
        super(Actions.empty());
    }

    public Action0Transform(Action0 action) {
        super(action);
    }

    public Action0Transform(Action0Transform action) {
        super(action.function);
    }

    public static final Action0Transform empty() {
        return EMPTY;
    }

    @Override protected final void invoke(Object... args) {
        if (args.length != 0) {
            throw new RuntimeException("Action0 expecting 0 arguments.");
        }
        function.call();
    }

    @Override public final void call() {
        function.call();
    }
}
