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

package sourcerer.internal;

import sourcerer.ExtensionMethod;
import sourcerer.InstanceModuleType;
import sourcerer.StaticModuleType;

import static sourcerer.ExtensionMethod.Kind.Instance;
import static sourcerer.ExtensionMethod.Kind.Return;
import static sourcerer.ExtensionMethod.Kind.ReturnThis;

@InstanceModuleType
@StaticModuleType
public class ModuleImpl {
    private static final ModuleImpl INSTANCE = new ModuleImpl();

    private volatile Object context;

    private ModuleImpl() {}

    @ExtensionMethod(Instance) public static ModuleImpl instance() {
        return INSTANCE;
    }

    @ExtensionMethod(ReturnThis) public ModuleImpl init(Object context) {
        if (this.context == null && context != null) {
            this.context = context;
        }
        return this;
    }

    @ExtensionMethod(Return) public Object context() {
        return context;
    }
}
