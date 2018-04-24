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

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.laynemobile.proxy.AnnotatedProxyElement;
import com.laynemobile.proxy.ProxyElement;
import com.laynemobile.proxy.ProxyEnv;
import com.laynemobile.proxy.ProxyFunctionElement;
import com.laynemobile.proxy.ProxyRound;
import com.laynemobile.proxy.ProxyType;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import static com.laynemobile.proxy.Util.typeMirrorArray;

public class ProxyFunctionOutput {
    private final AnnotatedProxyElement parent;
    private final ProxyFunctionElement element;
    private ProxyFunctionAbstractTypeOutputStub abstractTypeOutputStub;
    private ProxyFunctionAbstractTypeOutput abstractTypeOutput;
    private ProxyFunctionTypeOutputStub typeOutputStub;
    private TypeElementOutput typeOutput;
    private boolean finished;

    ProxyFunctionOutput(AnnotatedProxyElement parent, ProxyFunctionElement element) {
        this.parent = parent;
        this.element = element;
    }

    public AnnotatedProxyElement parent() {
        return parent;
    }

    public ProxyFunctionElement element() {
        return element;
    }

    public ProxyFunctionAbstractTypeOutputStub abstractTypeOutputStub() {
        return abstractTypeOutputStub;
    }

    public ProxyFunctionAbstractTypeOutput abstractTypeOutput() {
        return abstractTypeOutput;
    }

    public ProxyFunctionTypeOutputStub typeOutputStub() {
        return typeOutputStub;
    }

    public TypeElementOutput typeOutput() {
        return typeOutput;
    }

    public synchronized boolean isFinished() {
        return finished;
    }

    public synchronized TypeElementOutput nextOutput(ProxyRound.Input input)
            throws IOException {
        ProxyEnv env = input.env();
        TypeElementOutput output = null;
        if (abstractTypeOutputStub == null) {
            TypeElementOutputStub stub = firstOutputStub(input, element.outputStub());
            if (stub instanceof ProxyFunctionTypeOutputStub) {
                finished = true;
                // skip abstract type
                typeOutputStub = (ProxyFunctionTypeOutputStub) stub;
                output = typeOutput = typeOutput(input, typeOutputStub);
            } else {
                abstractTypeOutputStub = (ProxyFunctionAbstractTypeOutputStub) stub;
                output = abstractTypeOutput = abstractTypeOutputStub.writeTo(env);
            }
        } else if (typeOutputStub == null) {
            finished = true;
            if (abstractTypeOutput.hasOutput()) {
                typeOutputStub = abstractTypeOutput.outputStub(env);
                output = typeOutput = typeOutput(input, typeOutputStub);
            }
        }
        return output;
    }

    private static TypeElementOutput typeOutput(ProxyRound.Input input, ProxyFunctionTypeOutputStub typeOutputStub)
            throws IOException {
        if (typeOutputStub.elementExists(input.env())) {
            // already created
            return AbstractTypeElementOutput.existing(typeOutputStub);
        }
        return typeOutputStub.writeTo(input.env());
    }

    private static ProxyFunctionOutput output(ProxyFunctionElement element,
            Map<AnnotatedProxyElement, ImmutableSet<ProxyFunctionOutput>> inputFunctions) {
        ProxyElement parentElement = element.parent();
        for (Map.Entry<AnnotatedProxyElement, ImmutableSet<ProxyFunctionOutput>> entry : inputFunctions.entrySet()) {
            if (parentElement.equals(entry.getKey().element())) {
                for (ProxyFunctionOutput functionOutput : entry.getValue()) {
                    if (element.equals(functionOutput.element())) {
                        return functionOutput;
                    }
                }
                break;
            }
        }
        return null;
    }

    private TypeElementOutputStub firstOutputStub(ProxyRound.Input input,
            ProxyFunctionAbstractTypeOutputStub outputStub) {
        final ProxyEnv env = input.env();
        final AnnotatedProxyElement parent = this.parent;
        final ImmutableMap<AnnotatedProxyElement, ImmutableSet<ProxyFunctionOutput>> inputFunctions = input.allInputFunctions();
        for (ProxyFunctionElement override : element.overrides()) {
            ProxyElement overrideParentElement = override.parent();
            ProxyFunctionOutput functionOutput = output(override, inputFunctions);
            if (functionOutput == null) {
                continue;
            }

            ProxyType overrideParentType = null;
            for (ProxyType test : parent.element().directDependencies()) {
                if (test.element().equals(overrideParentElement)) {
                    overrideParentType = test;
                    break;
                }
            }
            if (overrideParentType == null) {
                continue;
            }

            ProxyFunctionTypeOutputStub generated = functionOutput.typeOutputStub;
            TypeMirror[] typeParams = typeMirrorArray(overrideParentType.type().actual().getTypeArguments());
            final TypeElement superElement = generated.element(env);
            env.log("super proxy element '%s', type parameters: '%s'", superElement, Arrays.toString(typeParams));
            if (superElement == null) {
                continue;
            }

            env.log("say man");
            env.log("%s -- writing override '%s' from '%s' -- %s", parent.toDebugString(),
                    outputStub.qualifiedName(), override, generated);

            DeclaredType superType = env.types()
                    .getDeclaredType(superElement, typeParams);
            env.log("super proxy type '%s'", superType);
            return new ProxyFunctionTypeOutputStub(outputStub, superType, env);
        }
        return outputStub;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProxyFunctionOutput)) return false;
        ProxyFunctionOutput that = (ProxyFunctionOutput) o;
        return Objects.equal(element, that.element);
    }

    @Override public int hashCode() {
        return Objects.hashCode(element);
    }
}
