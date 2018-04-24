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

package com.laynemobile.proxy.model.output;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.laynemobile.proxy.Util.Transformer;
import com.laynemobile.proxy.model.AnnotatedProxyElement;
import com.laynemobile.proxy.model.ProxyEnv;
import com.laynemobile.proxy.model.ProxyFunctionElement;
import com.laynemobile.proxy.model.ProxyRound;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.ElementKind;

import static com.laynemobile.proxy.Util.buildSet;

public class ProxyElementOutput {
    private final AnnotatedProxyElement element;
    private final ImmutableSet<ProxyFunctionOutput> outputs;
    private ProxyHandlerBuilderOutputStub handlerBuilderOutputStub;
    private TypeElementOutput handlerBuilderOutput;

    private ProxyElementOutput(final AnnotatedProxyElement element) {
        this.element = element;
        this.outputs = buildSet(element.functions(), new Transformer<ProxyFunctionOutput, ProxyFunctionElement>() {
            @Override public ProxyFunctionOutput transform(ProxyFunctionElement proxyFunctionElement) {
                return new ProxyFunctionOutput(element, proxyFunctionElement);
            }
        });
    }

    public static ProxyElementOutput create(AnnotatedProxyElement element) {
        return new ProxyElementOutput(element);
    }

    public AnnotatedProxyElement element() {
        return element;
    }

    public ImmutableSet<ProxyFunctionOutput> outputs() {
        return outputs;
    }

    public boolean isFinished() {
        for (ProxyFunctionOutput output : outputs) {
            if (!output.isFinished()) {
                return false;
            }
        }
        synchronized (this) {
            return handlerBuilderOutputStub != null;
        }
    }

    public synchronized ImmutableSet<TypeElementOutput> nextOutputs(ProxyRound.Input input)
            throws IOException {
        ProxyEnv env = input.env();
        boolean functionsFinished = true;
        Set<TypeElementOutput> out = new HashSet<>(outputs.size());
        for (ProxyFunctionOutput functionOutput : outputs) {
            if (!functionOutput.isFinished()) {
                functionsFinished = false;
                TypeElementOutput output = functionOutput.nextOutput(input);
                if (output != null) {
                    out.add(output);
                }
            }
        }
        if (functionsFinished && out.isEmpty()) {
            if (handlerBuilderOutputStub == null) {
                handlerBuilderOutputStub = ProxyHandlerBuilderOutputStub.create(env, element, outputs);
                if (element.element().element().getKind() == ElementKind.INTERFACE) {
                    handlerBuilderOutput = handlerBuilderOutputStub.writeTo(env);
                    if (handlerBuilderOutput != null) {
                        out.add(handlerBuilderOutput);
                    }
                }
            }
        }
        return ImmutableSet.copyOf(out);
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProxyElementOutput)) return false;
        ProxyElementOutput that = (ProxyElementOutput) o;
        return Objects.equal(element, that.element);
    }

    @Override public int hashCode() {
        return Objects.hashCode(element);
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("element", element)
                .toString();
    }
}
