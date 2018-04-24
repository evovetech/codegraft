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

package com.laynemobile.proxy.model;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableCollection.Builder;
import com.google.common.collect.ImmutableSet;
import com.laynemobile.proxy.Util.Collector;
import com.laynemobile.proxy.model.output.ProxyElementOutput;
import com.laynemobile.proxy.model.output.TypeElementOutput;

import java.io.IOException;

import sourcerer.processor.Env;

import static com.laynemobile.proxy.Util.buildSet;

public class ProxyElementRound extends EnvRound<ProxyElementRound> {
    private final ProxyElementOutput elementOutput;
    private final ImmutableSet<TypeElementOutput> outputs;

    private ProxyElementRound(Env env, ProxyElementOutput elementOutput) {
        super(env);
        this.elementOutput = elementOutput;
        this.outputs = ImmutableSet.of();
    }

    private ProxyElementRound(ProxyElementRound previous, ImmutableSet<TypeElementOutput> outputs) {
        super(previous);
        this.elementOutput = previous.elementOutput;
        this.outputs = outputs;
    }

    public static ProxyElementRound create(AnnotatedProxyElement element, Env env) {
        ProxyElementOutput output = ProxyElementOutput.create(element);
        return new ProxyElementRound(env, output);
    }

    public ProxyElementOutput elementOutput() {
        return elementOutput;
    }

    public AnnotatedProxyElement element() {
        return elementOutput.element();
    }

    public ImmutableSet<TypeElementOutput> outputs() {
        return outputs;
    }

    public ImmutableSet<TypeElementOutput> allOutputs() {
        return buildSet(allRounds(), new Collector<TypeElementOutput, ProxyElementRound>() {
            @Override public void collect(ProxyElementRound round, Builder<TypeElementOutput> out) {
                out.addAll(round.outputs());
            }
        });
    }

    public boolean isFinished() {
        return elementOutput.isFinished();
    }

    public ProxyElementRound nextRound(ProxyRound.Input input) throws IOException {
        return new ProxyElementRound(this, elementOutput.nextOutputs(input));
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("\nelementOutput", elementOutput)
                .add("\noutputs", outputs)
                .toString();
    }
}
