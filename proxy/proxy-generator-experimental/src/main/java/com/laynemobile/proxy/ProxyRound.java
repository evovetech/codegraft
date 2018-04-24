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

package com.laynemobile.proxy;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.laynemobile.proxy.Util.Collector;
import com.laynemobile.proxy.Util.Transformer;
import com.laynemobile.proxy.output.ProxyElementOutput;
import com.laynemobile.proxy.output.ProxyFunctionOutput;
import com.laynemobile.proxy.output.TypeElementOutput;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import sourcerer.processor.Env;

import static com.laynemobile.proxy.Util.buildSet;
import static com.laynemobile.proxy.Util.combine;

public class ProxyRound extends EnvRound<ProxyRound> {
    private final Input input;
    private final Output output;

    private ProxyRound(Env env) {
        super(env);
        this.input = new Input(env);
        this.output = new Output();
    }

    private ProxyRound(ProxyRound previous, Input input, Output output) {
        super(previous);
        this.input = input;
        this.output = output;
    }

    static ProxyRound begin(Env env) {
        return new ProxyRound(env);
    }

    ProxyRound process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws IOException {
        // add all processed elements from current round
        Output lastOutput = output;
        Input newInput = input.process(annotations, roundEnv, lastOutput);
        Output newOutput = lastOutput.write(newInput);
        ProxyRound nextRound = new ProxyRound(this, newInput, newOutput);
        if (!newOutput.outputRounds().isEmpty() && !newOutput.didWrite()) {
            log("round=%s", nextRound);
            return nextRound.process(annotations, roundEnv);
        }
        return nextRound;
    }

    public Input input() {
        return input;
    }

    public Output output() {
        return output;
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("\nround", round())
                .add("\ninput", doubleIndent(input))
                .add("\noutput", doubleIndent(output))
                .toString();
    }

    private static boolean isInList(ProxyElement proxyElement, Set<ProxyElementOutput> list) {
        for (ProxyElementOutput item : list) {
            if (item.element().equals(proxyElement)) {
                return true;
            }
        }
        return false;
    }

    public static final class Input extends EnvRound<Input> {
        private final ImmutableSet<? extends TypeElement> annotations;
        private final ImmutableSet<? extends Element> rootElements;
        private final ImmutableSet<AnnotatedProxyElement> annotatedElements;
        private final ImmutableSet<AnnotatedProxyElement> outputElements;
        private final ImmutableSet<ProxyElementRound> inputRounds;

        private Input(Env env) {
            super(env);
            this.annotations = ImmutableSet.of();
            this.rootElements = ImmutableSet.of();
            this.annotatedElements = ImmutableSet.of();
            this.outputElements = ImmutableSet.of();
            this.inputRounds = ImmutableSet.of();
        }

        private Input(Input previous, Set<? extends TypeElement> annotations, Set<? extends Element> rootElements,
                Set<? extends AnnotatedProxyElement> annotatedElements,
                Set<? extends AnnotatedProxyElement> outputElements,
                Set<ProxyElementRound> inputRounds) {
            super(previous);
            this.annotations = ImmutableSet.copyOf(annotations);
            this.rootElements = ImmutableSet.copyOf(rootElements);
            this.annotatedElements = ImmutableSet.copyOf(annotatedElements);
            this.outputElements = ImmutableSet.copyOf(outputElements);
            this.inputRounds = ImmutableSet.copyOf(inputRounds);
        }

        public ImmutableSet<? extends TypeElement> annotations() {
            return annotations;
        }

        public ImmutableSet<? extends Element> rootElements() {
            return rootElements;
        }

        public ImmutableSet<AnnotatedProxyElement> annotatedElements() {
            return annotatedElements;
        }

        public ImmutableSet<AnnotatedProxyElement> outputElements() {
            return outputElements;
        }

        public ImmutableSet<ProxyElementRound> inputRounds() {
            return inputRounds;
        }

        public ImmutableMap<AnnotatedProxyElement, ImmutableSet<TypeElementOutput>> inputs() {
            ImmutableMap.Builder<AnnotatedProxyElement, ImmutableSet<TypeElementOutput>> out
                    = ImmutableMap.builder();
            for (ProxyElementRound inputRound : inputRounds) {
                out.put(inputRound.element(), inputRound.outputs());
            }
            return out.build();
        }

        public ImmutableSet<ProxyElementOutput> allInputElements() {
            return buildSet(allRounds(), new Collector<ProxyElementOutput, Input>() {
                @Override public void collect(Input input, ImmutableCollection.Builder<ProxyElementOutput> out) {
                    for (ProxyElementRound inputRound : input.inputRounds) {
                        out.add(inputRound.elementOutput());
                    }
                }
            });
        }

        public ImmutableMap<AnnotatedProxyElement, ImmutableSet<ProxyFunctionOutput>> allInputFunctions() {
            ImmutableMap.Builder<AnnotatedProxyElement, ImmutableSet<ProxyFunctionOutput>> out
                    = ImmutableMap.builder();
            for (ProxyElementOutput elementOutput : allInputElements()) {
                out.put(elementOutput.element(), elementOutput.outputs());
            }
            return out.build();
        }

        public ImmutableSet<Element> allRootElements() {
            return buildSet(allRounds(), new Collector<Element, Input>() {
                @Override public void collect(Input input, ImmutableCollection.Builder<Element> out) {
                    out.addAll(input.rootElements);
                }
            });
        }

        public ImmutableSet<AnnotatedProxyElement> allAnnotatedElements() {
            return buildSet(allRounds(), new Collector<AnnotatedProxyElement, Input>() {
                @Override public void collect(Input input, ImmutableCollection.Builder<AnnotatedProxyElement> out) {
                    out.addAll(input.annotatedElements);
                }
            });
        }

        public ImmutableSet<AnnotatedProxyElement> allOutputElements() {
            return buildSet(allRounds(), new Collector<AnnotatedProxyElement, Input>() {
                @Override public void collect(Input input, ImmutableCollection.Builder<AnnotatedProxyElement> out) {
                    out.addAll(input.outputElements);
                }
            });
        }

        public ImmutableMap<AnnotatedProxyElement, ImmutableSet<TypeElementOutput>> allInputs() {
            Input previous = previous();
            if (previous == null) {
                return inputs();
            }
            return combine(previous.allInputs(), inputs());
        }

        public ImmutableSet<AnnotatedProxyElement> allProcessedElements() {
            Input previous = previous();
            if (previous == null) {
                return ImmutableSet.of();
            }
            return buildSet(previous.allRounds(), new Collector<AnnotatedProxyElement, Input>() {
                @Override public void collect(Input input, ImmutableCollection.Builder<AnnotatedProxyElement> out) {
                    out.addAll(input.outputElements);
                }
            });
        }

        private ImmutableSet<AnnotatedProxyElement> unprocessed(Set<AnnotatedProxyElement> annotatedElements) {
            Set<AnnotatedProxyElement> processedElements = allOutputElements();
            ImmutableSet.Builder<AnnotatedProxyElement> unprocessed = ImmutableSet.builder();
            for (AnnotatedProxyElement annotatedElement : annotatedElements) {
                if (!processedElements.contains(annotatedElement)) {
                    unprocessed.add(annotatedElement);
                }
                for (AnnotatedProxyType dependency : annotatedElement.allAnnotatedDependencies()) {
                    AnnotatedProxyElement element = dependency.element();
                    if (!processedElements.contains(element)) {
                        unprocessed.add(element);
                    }
                }
            }
            return unprocessed.build();
        }

        private Input process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv, Output lastOutput)
                throws IOException {
            final ProxyEnv env = env();
            final ImmutableSet<AnnotatedProxyElement> curAnnotatedElements
                    = AnnotatedProxyElement.process(env, roundEnv);
            final ImmutableSet<AnnotatedProxyElement> allAnnotatedElements = ImmutableSet.<AnnotatedProxyElement>builder()
                    .addAll(allAnnotatedElements())
                    .addAll(curAnnotatedElements)
                    .build();
            final ImmutableSet<ProxyElementRound> inputRounds = lastOutput.nextInputRound(this);
            log("all annotated proxy elements: %s", allAnnotatedElements);

            final ImmutableSet<AnnotatedProxyElement> unprocessedElements = unprocessed(allAnnotatedElements);
            log("all unprocessed elements: %s", unprocessedElements);
            final Set<AnnotatedProxyElement> processedElements = new HashSet<>(allOutputElements());
            for (ProxyElementRound inputRound : inputRounds) {
                if (!inputRound.isFinished()) {
                    processedElements.remove(inputRound.element());
                }
            }
            log("all processed elements: %s", processedElements);

            final Set<AnnotatedProxyType> dependencies
                    = buildSet(unprocessedElements, new Collector<AnnotatedProxyType, AnnotatedProxyElement>() {
                @Override
                public void collect(AnnotatedProxyElement unprocessed,
                        ImmutableCollection.Builder<AnnotatedProxyType> out) {
                    for (AnnotatedProxyType dependency : unprocessed.allAnnotatedDependencies()) {
                        if (!processedElements.contains(dependency.element())) {
                            out.add(dependency);
                        }
                    }
                }
            });

            log("dependencies: %s", dependencies);

            Set<AnnotatedProxyElement> round
                    = buildSet(unprocessedElements, new Transformer<AnnotatedProxyElement, AnnotatedProxyElement>() {
                @Override public AnnotatedProxyElement transform(AnnotatedProxyElement unprocessed) {
                    for (AnnotatedProxyType dependency : unprocessed.allAnnotatedDependencies()) {
                        if (dependencies.contains(dependency)) {
                            return null;
                        }
                    }
                    return unprocessed;
                }
            });

            log("round: %s", round);

            return new Input(this, annotations, roundEnv.getRootElements(), curAnnotatedElements, round, inputRounds);
        }

        @Override public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("\nannotations", annotations)
                    .add("\nrootElements", rootElements)
                    .add("\nannotatedElements", annotatedElements)
                    .add("\noutputElements", outputElements)
                    .toString();
        }
    }

    public static final class Output extends AbstractRound<Output> {
        private final ImmutableSet<ProxyElementRound> outputRounds;

        private Output() {
            this.outputRounds = ImmutableSet.of();
        }

        private Output(Output previous, Set<ProxyElementRound> outputRounds) {
            super(previous);
            this.outputRounds = ImmutableSet.copyOf(outputRounds);
        }

        Output write(Input input) throws IOException {
            final ProxyEnv env = input.env();
            final Set<ProxyElementRound> outputRounds = new HashSet<>(input.inputRounds());

            for (AnnotatedProxyElement proxyElement : input.outputElements()) {
                outputRounds.add(ProxyElementRound.create(proxyElement, env)
                        .nextRound(input));
            }

            return new Output(this, outputRounds);
        }

        public ImmutableSet<ProxyElementRound> outputRounds() {
            return outputRounds;
        }

        public ImmutableMap<AnnotatedProxyElement, ImmutableSet<TypeElementOutput>> outputs() {
            ImmutableMap.Builder<AnnotatedProxyElement, ImmutableSet<TypeElementOutput>> out
                    = ImmutableMap.builder();
            for (ProxyElementRound outputRound : outputRounds) {
                out.put(outputRound.element(), outputRound.outputs());
            }
            return out.build();
        }

        public ImmutableMap<AnnotatedProxyElement, ImmutableSet<TypeElementOutput>> allOutputs() {
            Output previous = previous();
            if (previous == null) {
                return outputs();
            }
            return combine(previous.outputs(), outputs());
        }

        public ImmutableSet<ProxyElementRound> nextInputRound(final Input input) throws IOException {
            return buildSet(outputRounds, new Transformer<ProxyElementRound, ProxyElementRound>() {
                @Override public ProxyElementRound transform(ProxyElementRound proxyElementRound) {
                    if (!proxyElementRound.isFinished()) {
                        try {
                            return proxyElementRound.nextRound(input);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return null;
                }
            });
        }

        public boolean didWrite() {
            for (Set<TypeElementOutput> set : outputs().values()) {
                for (TypeElementOutput output : set) {
                    if (output.didWrite()) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("\noutputRounds", outputRounds)
                    .toString();
        }
    }

    static String doubleIndent(Object o) {
        return doubleIndent(o == null ? null : o.toString());
    }

    static String doubleIndent(String value) {
        if (value == null) {
            return "null";
        }
        return value.replace("\n", "\n    ");
    }
}
