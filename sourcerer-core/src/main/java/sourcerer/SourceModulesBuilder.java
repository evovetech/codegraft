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

public abstract class SourceModulesBuilder<T> implements SourceModuleBuilder<T> {
    public abstract TypeHandler<T>[] buildModules();

    @Override public final TypeHandler<T> build() {
        TypeHandler<T>[] modules = buildModules();
        if (modules.length == 1) {
            return modules[0];
        }
        throw new UnsupportedOperationException("this class builds more than one module");
    }
}
