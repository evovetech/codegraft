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

package com.laynemobile.proxy.processor;

import com.laynemobile.proxy.Builder;
import com.laynemobile.proxy.ProxyBuilder;

import java.util.ArrayList;
import java.util.List;

public final class ProcessorBuilder<T, R, PROXY> implements Builder<Processor<T, R>> {
    private final ProxyBuilder<PROXY> proxyBuilder;
    private final ProcessorHandler.Parent<T, R, PROXY> parent;
    private final List<ProcessorHandler<T, R, ? extends PROXY>> handlers = new ArrayList<>();

    private ProcessorBuilder(ProcessorHandler.Parent<T, R, PROXY> parent) {
        if (parent == null) {
            throw new IllegalArgumentException("parent cannot be null");
        }
        this.proxyBuilder = new ProxyBuilder<>(parent.proxyHandler());
        this.parent = parent;
    }

    public static <T, R, PROXY> ProcessorBuilder<T, R, PROXY> create(ProcessorHandler.Parent<T, R, PROXY> parent) {
        return new ProcessorBuilder<>(parent);
    }

    public ProcessorBuilder<T, R, PROXY> add(ProcessorHandler<T, R, ? extends PROXY> handler) {
        proxyBuilder.add(handler.proxyHandler());
        if (handler instanceof ProcessorHandler.Parent) {
            throw new IllegalArgumentException("can only have one parent");
        } else {
            handlers.add(handler);
        }
        return this;
    }

    public ProcessorBuilder<T, R, PROXY> addAll(ProcessorHandler<T, R, ? extends PROXY>... handlers) {
        for (ProcessorHandler<T, R, ? extends PROXY> handler : handlers) {
            add(handler);
        }
        return this;
    }

    public ProcessorBuilder<T, R, PROXY> addAll(List<ProcessorHandler<T, R, ? extends PROXY>> handlers) {
        for (ProcessorHandler<T, R, ? extends PROXY> handler : handlers) {
            add(handler);
        }
        return this;
    }

    @Override public final Processor<T, R> build() {
        final PROXY proxy = proxyBuilder.build();
        final ImmutableInterceptProcessor.Builder<T, R> builder = ImmutableInterceptProcessor.<T, R>builder()
                .setProcessor(parent.extension(proxy));

        for (ProcessorHandler<T, R, ? extends PROXY> handler : handlers) {
            Processor.Extension<T, R> extension = extension(handler, proxy);
            if (extension instanceof Processor.Checker) {
                builder.addCheckers((Processor.Checker<T, R>) extension);
            } else if (extension instanceof Processor.Modifier) {
                builder.addModifiers((Processor.Modifier<T, R>) extension);
            } else if (extension instanceof Processor.Interceptor) {
                builder.addInterceptors((Processor.Interceptor<T, R>) extension);
            } else {
                throw new IllegalArgumentException("unknown Extension type: " + extension);
            }
        }

        return builder.build();
    }

    @SuppressWarnings("unchecked")
    private <E extends PROXY> Processor.Extension<T, R> extension(ProcessorHandler<T, R, E> handler, PROXY proxy) {
        return handler.extension((E) proxy);
    }
}
