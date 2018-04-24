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

package com.laynemobile.api;

import com.laynemobile.api.functions.AggregableProxy_keepAliveOnError;
import com.laynemobile.api.functions.AggregableProxy_keepAliveSeconds;
import com.laynemobile.api.functions.AggregableProxy_key;
import com.laynemobile.proxy.NamedMethodHandler;
import com.laynemobile.proxy.ProxyCompleter;
import com.laynemobile.proxy.ProxyHandler;
import com.laynemobile.proxy.TypeToken;
import com.laynemobile.proxy.annotations.Generated;
import com.laynemobile.proxy.functions.Func0;
import com.laynemobile.proxy.functions.Func1;
import com.laynemobile.proxy.functions.Function;
import com.laynemobile.proxy.functions.ProxyFunction;

import static com.laynemobile.proxy.AbstractProxyHandlerBuilder.handler;

@Generated
public class AggregableProxyHandlerBuilder2 {
    public Step1 setKey(AggregableProxy_key key) {
        return new Step1(key);
    }

    public Step1 setKey(Func0<Object> key) {
        return new Step1(new AggregableProxy_key(key));
    }

    public interface State {}

    public static abstract class AbstractStep<S extends State, N> {
        protected final S state;

        protected AbstractStep(S state) {
            this.state = state;
        }

        protected abstract N next();
    }

    public static abstract class FinalStep<S extends State, C extends ProxyCompleter<?>> extends AbstractStep<S, C> {
        protected FinalStep(S state) {
            super(state);
        }
    }

    public static final class FunctionHolder<F extends Function> {
        public final NamedMethodHandler handler;
        public final F function;

        public FunctionHolder(ProxyFunction<F> proxyFunction) {
            this.handler = handler(proxyFunction);
            this.function = proxyFunction.function();
        }
    }

    public static class State1 implements State {
        public final FunctionHolder<Func0<Object>> key;

        private State1(State1 state1) {
            this.key = state1.key;
        }

        private State1(AggregableProxy_key key) {
            this.key = new FunctionHolder<>(key);
        }

        public final class KeepAliveSeconds extends AggregableProxy_keepAliveSeconds {
            public KeepAliveSeconds(final Func1<State1, Integer> keepAliveSeconds) {
                super(new Func0<Integer>() {
                    @Override public Integer call() {
                        return keepAliveSeconds.call(State1.this);
                    }
                });
            }
        }
    }

    public static final class Step1 extends AbstractStep<State1, Step2> {
        private AggregableProxy_keepAliveSeconds keepAliveSeconds;

        private Step1(AggregableProxy_key key) {
            super(new State1(key));
        }

        public Step2 setKeepAliveSeconds(AggregableProxy_keepAliveSeconds keepAliveSeconds) {
            this.keepAliveSeconds = keepAliveSeconds;
            return next();
        }

        public Step2 setKeepAliveSeconds(Func1<State1, Integer> keepAliveSeconds) {
            this.keepAliveSeconds = state.new KeepAliveSeconds(keepAliveSeconds);
            return next();
        }

        @Override protected Step2 next() {
            return new Step2(state, keepAliveSeconds);
        }
    }

    public static final class State2 extends State1 {
        public final FunctionHolder<Func0<Integer>> keepAliveSeconds;

        private State2(State1 state, AggregableProxy_keepAliveSeconds keepAliveSeconds) {
            super(state);
            this.keepAliveSeconds = new FunctionHolder<>(keepAliveSeconds);
        }

        public final class KeepAliveOnError extends AggregableProxy_keepAliveOnError {
            public KeepAliveOnError(final Func1<State2, Boolean> keepAliveOnError) {
                super(new Func0<Boolean>() {
                    @Override public Boolean call() {
                        return keepAliveOnError.call(State2.this);
                    }
                });
            }
        }
    }

    public static final class Step2 extends FinalStep<State2, ProxyCompleter<Aggregable>> {
        private AggregableProxy_keepAliveOnError keepAliveOnError;

        private Step2(State1 state, AggregableProxy_keepAliveSeconds keepAliveSeconds) {
            super(new State2(state, keepAliveSeconds));
        }

        public ProxyCompleter<Aggregable> setKeepAliveOnError(AggregableProxy_keepAliveOnError keepAliveOnError) {
            this.keepAliveOnError = keepAliveOnError;
            return next();
        }

        public ProxyCompleter<Aggregable> setKeepAliveOnError(Func1<State2, Boolean> keepAliveOnError) {
            this.keepAliveOnError = state.new KeepAliveOnError(keepAliveOnError);
            return next();
        }

        private ProxyHandler<Aggregable> proxyHandler() {
            return ProxyHandler.builder(new TypeToken<Aggregable>() {})
                    .handle(state.key.handler)
                    .handle(state.keepAliveSeconds.handler)
                    .handle(handler(keepAliveOnError))
                    .build();
        }

        @Override protected ProxyCompleter<Aggregable> next() {
            return new ProxyCompleter<>(proxyHandler());
        }
    }
}
