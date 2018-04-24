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

package com.laynemobile.proxy.output;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.TypeElement;

import sourcerer.processor.Env;

public abstract class AbstractTypeElementOutput<S extends TypeElementOutputStub> implements TypeElementOutput {
    private final S source;
    private final TypeSpec typeSpec;
    private final boolean didWrite;

    protected AbstractTypeElementOutput(S source, TypeSpec typeSpec, boolean didWrite) {
        this.source = source;
        this.typeSpec = typeSpec;
        this.didWrite = didWrite;
    }

    static TypeElementOutput existing(TypeElementOutputStub source) {
        return create(source, null, false);
    }

    static TypeElementOutput create(TypeElementOutputStub source, TypeSpec typeSpec, boolean didWrite) {
        return new AbstractTypeElementOutput<TypeElementOutputStub>(source, typeSpec, didWrite) {
            @Override public boolean hasOutput() {
                return false;
            }
        };
    }

    @Override public final S source() {
        return source;
    }

    @Override public final TypeSpec typeSpec() {
        return typeSpec;
    }

    @Override public final boolean didWrite() {
        return didWrite;
    }

    @Override public TypeElement element(Env env) {
        return source.element(env);
    }

    @Override public boolean hasOutput() {
        return false;
    }

    @Override public TypeElementOutputStub outputStub(Env env) {
        if (hasOutput()) {
            throw new IllegalStateException("must return outputStub if hasOutput() returns true");
        }
        return null;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractTypeElementOutput)) return false;
        AbstractTypeElementOutput that = (AbstractTypeElementOutput) o;
        return Objects.equal(source, that.source);
    }

    @Override public int hashCode() {
        return Objects.hashCode(source);
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("source", source)
                .toString();
    }
}
