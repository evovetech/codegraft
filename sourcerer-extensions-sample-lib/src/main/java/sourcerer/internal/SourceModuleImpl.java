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
import sourcerer.Module;

@Module
public class SourceModuleImpl {
    private static final SourceModuleImpl INSTANCE = new SourceModuleImpl();

    private volatile Object context;

    private SourceModuleImpl() {}

    @ExtensionMethod(ExtensionMethod.Kind.Instance) public static SourceModuleImpl instance() {
        return INSTANCE;
    }

    @ExtensionMethod(ExtensionMethod.Kind.ReturnThis) public SourceModuleImpl init(Object context) {
        if (this.context == null && context != null) {
            this.context = context;
        }
        return this;
    }

    @ExtensionMethod(ExtensionMethod.Kind.Return) public Object context() {
        return context;
    }
}
