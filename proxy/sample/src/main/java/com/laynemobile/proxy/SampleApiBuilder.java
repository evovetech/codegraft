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

import com.laynemobile.proxy.functions.Func1;
import com.laynemobile.proxy.processor.Processor;
import com.laynemobile.proxy.processor.ProcessorBuilder;
import com.laynemobile.proxy.processor.ProcessorHandler;

import java.util.List;

public final class SampleApiBuilder<T, R> {

    public SampleApiBuilder() {}

    public Extensions setSource(Func1<T, R> source) {
        return new TestProxyHandlerModuleBuilder()
                .setSource(source)
                .add();
    }

    // Parent Builder first step, which returns 2nd (optional) extensions variation
    public final class TestProxyHandlerModuleBuilder {
        private final TestInterfaceProcessorHandlerBuilder<T, R> builder = new TestInterfaceProcessorHandlerBuilder<>();

        private TestProxyHandlerModuleBuilder() {}

        public TestProxyHandlerModuleBuilder setSource(Func1<T, R> source) {
            builder.setSource(source);
            return this;
        }

        public Extensions add() {
            return new Extensions(builder.build());
        }
    }

    public final class Extensions implements Builder<Processor<T, R>> {
        private final ProcessorBuilder<T, R, TestInterface<T, R>> builder;

        private Extensions(ProcessorHandler.Parent<T, R, TestInterface<T, R>> parent) {
            this.builder = ProcessorBuilder.create(parent);
        }

        public Extensions add(ProcessorHandler<T, R, ? extends TestInterface<T, R>> handler) {
            builder.add(handler);
            return this;
        }

        public Extensions addAll(ProcessorHandler<T, R, ? extends TestInterface<T, R>>... handlers) {
            builder.addAll(handlers);
            return this;
        }

        public Extensions addAll(List<ProcessorHandler<T, R, ? extends TestInterface<T, R>>> handlers) {
            builder.addAll(handlers);
            return this;
        }

        @Override public Processor<T, R> build() {
            return builder.build();
        }
    }
}

