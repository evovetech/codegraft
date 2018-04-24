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

import com.laynemobile.proxy.annotations.ProxyHandlerModule;
import com.laynemobile.proxy.functions.Func1;
import com.laynemobile.proxy.processor.ErrorHandler;
import com.laynemobile.proxy.processor.Processor;
import com.laynemobile.proxy.processor.ProcessorHandler;

@ProxyHandlerModule
public class TestInterfaceProcessorHandlerBuilder<T, R> implements Builder<ProcessorHandler.Parent<T, R, TestInterface<T, R>>> {
    private final TestInterfaceProxyHandlerBuilder<T, R> builder = new TestInterfaceProxyHandlerBuilder<>();

    public void setSource(Func1<T, R> source) {
        builder.setSource(source);
    }

    @Override public ProcessorHandler.Parent<T, R, TestInterface<T, R>> build() {
        return build(builder.proxyHandler());
    }

    private static <T, R> ProcessorHandler.Parent<T, R, TestInterface<T, R>> build(
            final ProxyHandler<TestInterface<T, R>> proxyHandler) {
        return new ProcessorHandler.Parent<T, R, TestInterface<T, R>>() {
            @Override public ProxyHandler<TestInterface<T, R>> proxyHandler() {
                return proxyHandler;
            }

            @Override public Processor.Parent<T, R> extension(final TestInterface<T, R> testInterface) {
                return new Processor.Parent<T, R>() {
                    @Override public ErrorHandler<R> errorHandler() {
                        return new ErrorHandler<R>() {
                            @Override public R onError(Throwable throwable) {
                                return null;
                            }
                        };
                    }

                    @Override public R call(T t) {
                        return testInterface.call(t);
                    }
                };
            }
        };
    }
}
