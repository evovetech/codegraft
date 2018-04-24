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

import com.laynemobile.proxy.TypeToken;
import com.laynemobile.proxy.functions.transforms.Action0Transform;

public class Action0Def extends ActionDef<Action0Transform> {
    public Action0Def(String name) {
        super(name, new TypeToken<?>[0]);
    }

    @Override public Action asFunction(Action0Transform transform) {
        return new Action(this, transform);
    }

    public static class Action extends ProxyAction<Action0Transform> {
        protected Action(Action0Def actionDef, Action0Transform action) {
            super(actionDef, action);
        }
    }
}
