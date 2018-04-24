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

package com.laynemobile.proxy.types;

import com.google.common.collect.ImmutableList;

import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeVisitor;

final class DefaultExecutableTypeAlias extends AbstractTypeMirrorAlias<ExecutableType> implements ExecutableTypeAlias {
    private final ImmutableList<? extends TypeMirrorAlias> parameterTypes;
    private final TypeMirrorAlias returnType;
    private final ImmutableList<? extends TypeMirrorAlias> thrownTypes;
    private final ImmutableList<? extends TypeVariableAlias> typeVariables;

    private DefaultExecutableTypeAlias(ExecutableType typeMirror) {
        super(typeMirror);
        this.parameterTypes = AliasTypes.parameterTypes(typeMirror.getParameterTypes());
        this.returnType = AliasTypes.get(typeMirror.getReturnType());
        this.thrownTypes = AliasTypes.list(typeMirror.getThrownTypes());
        this.typeVariables = AliasTypes.typeVariables(typeMirror.getTypeVariables());
    }

    static ExecutableTypeAlias of(ExecutableType executableType) {
        if (executableType instanceof ExecutableTypeAlias) {
            return (ExecutableTypeAlias) executableType;
        }
        return new DefaultExecutableTypeAlias(executableType);
    }

    @Override public ImmutableList<? extends TypeMirrorAlias> getParameterTypes() {
        return parameterTypes;
    }

    @Override public TypeMirrorAlias getReturnType() {
        return returnType;
    }

    @Override public ImmutableList<? extends TypeMirrorAlias> getThrownTypes() {
        return thrownTypes;
    }

    @Override public ImmutableList<? extends TypeVariableAlias> getTypeVariables() {
        return typeVariables;
    }

    @Override public <R, P> R accept(TypeVisitor<R, P> v, P p) {
        return v.visitExecutable(actual(), p);
    }
}
