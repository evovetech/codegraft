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

import com.google.common.collect.ImmutableList;

import java.util.List;

public abstract class InterceptProcessor<T, R> implements Processor<T, R> {
    public abstract ErrorHandlerProcessor<T, R> processor();

    public abstract List<Checker<T, R>> checkers();

    public abstract List<Modifier<T, R>> modifiers();

    public abstract List<Interceptor<T, R>> interceptors();

    @Override public R call(T t) {
        return new ImmutableChain()
                .proceed(t);
    }

    private final class ImmutableProcessor implements Processor<T, R> {
        private final ErrorHandlerProcessor<T, R> processor;
        private final ErrorHandler<R> errorHandler;
        private final ImmutableList<Checker<T, R>> checkers;
        private final ImmutableList<Modifier<T, R>> modifiers;

        private ImmutableProcessor() {
            this.processor = processor();
            this.errorHandler = processor.errorHandler();
            this.checkers = ImmutableList.copyOf(checkers());
            this.modifiers = ImmutableList.copyOf(modifiers());
        }

        @Override public R call(T t) {
            // Validate with checkers
            for (Checker<T, R> checker : checkers) {
                try {
                    checker.check(t);
                } catch (Exception e) {
                    return errorHandler.onError(e);
                }
            }

            // Make actual call
            R result = processor.call(t);

            // Allow modifications to original result
            for (Modifier<T, R> modifier : modifiers) {
                result = modifier.modify(t, result);
            }

            // return potentially modified  result
            return result;
        }
    }

    private final class ImmutableChain implements Interceptor.Chain<T, R> {
        private final ImmutableProcessor processor;
        private final ImmutableList<Interceptor<T, R>> interceptors;
        private final int index;
        private final T value;

        private ImmutableChain() {
            this.processor = new ImmutableProcessor();
            this.interceptors = ImmutableList.copyOf(interceptors());
            this.index = 0;
            this.value = null;
        }

        private ImmutableChain(ImmutableChain prev, T t) {
            this.processor = prev.processor;
            this.interceptors = prev.interceptors;
            this.index = prev.index + 1;
            this.value = t;
        }

        @Override public T value() {
            return value;
        }

        @Override public R proceed(T t) {
            int index = this.index;
            List<Interceptor<T, R>> interceptors = this.interceptors;
            if (index < interceptors.size()) {
                return interceptors.get(index)
                        .intercept(next(t));
            }
            return processor.call(t);
        }

        private ImmutableChain next(T t) {
            return new ImmutableChain(this, t);
        }
    }
}
