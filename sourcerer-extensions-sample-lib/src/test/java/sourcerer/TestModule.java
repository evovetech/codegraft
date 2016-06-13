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

package sourcerer;

import sourcerer.ExtensionMethod.Kind;

@Module
public class TestModule {
    private static final TestModule INSTANCE = new TestModule();

    private volatile Object context;

    private TestModule() {}

    @ExtensionMethod(Kind.Instance) public static TestModule instance() {
        return INSTANCE;
    }

    @ExtensionMethod(Kind.ReturnThis) public TestModule init(Object context) {
        if (this.context == null && context != null) {
            this.context = context;
        }
        return this;
    }

    @ExtensionMethod(Kind.Return) public Object context() {
        return context;
    }
}
