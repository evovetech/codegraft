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

import com.squareup.javapoet.ClassName;

public final class ExtensionDescriptor {
    private final ExtensionClass.Kind kind;
    private final String packageName;
    private final String className;
    private final String qualifiedName;
    private final ClassName typeName;

    public ExtensionDescriptor(ExtensionClass.Kind kind, String packageName, String className) {
        this.kind = kind;
        this.packageName = packageName;
        this.className = className;
        this.qualifiedName = packageName + "." + className;
        this.typeName = ClassName.get(packageName, className);
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExtensionDescriptor)) return false;

        ExtensionDescriptor key = (ExtensionDescriptor) o;

        if (kind != key.kind) return false;
        return qualifiedName.equals(key.qualifiedName);
    }

    @Override public int hashCode() {
        int result = kind.hashCode();
        result = 31 * result + qualifiedName.hashCode();
        return result;
    }

    public ExtensionClass.Kind kind() {
        return kind;
    }

    public String packageName() {
        return packageName;
    }

    public String className() {
        return className;
    }

    public String qualifiedName() {
        return qualifiedName;
    }

    public ClassName typeName() {
        return typeName;
    }
}
