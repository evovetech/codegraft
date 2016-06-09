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

package sourcerer;

/** Marker interface for Source classes. */
public interface Source {

    enum Kind {
        InstanceDelegate,
        StaticDelegate
    }

    /*
    public interface RequestProcessor<T, P> {
        Request<T> request(P params);

        interface Interceptor<T, P> {
            Request<T> intercept(Chain<T, P> chain);

            interface Chain<T, P> {
                RequestProcessor<T, P> processor();

                Request<T> proceed(RequestProcessor<T, P> processor);
            }
        }
    }

    final List<RequestProcessor.Interceptor> interceptors;

    private Request getRequestWithInterceptorChain(P params) {
        RequestProcessor defaultProcessor = defaultProcessor();
        RequestProcessorInterceptorChain chain = new RequestProcessorInterceptorChain(0, defaultProcessor);
        RequestProcessor processor = chain.proceed(defaultProcessor);
        return processor.request(params);
    }

    class RequestProcessorInterceptorChain implements RequestProcessor.Interceptor.Chain {
        private final int index;
        private final RequestProcessor processor;

        RequestProcessorInterceptorChain(int index, RequestProcessor processor) {
            this.index = index;
            this.processor = processor;
        }

        public RequestProcessor processor() {
            return processor;
        }

        public Request<T> proceed(RequestProcessor<T, P> processor) {
            if (index < interceptors.size()) {
                RequestProcessorInterceptorChain chain = new RequestProcessorInterceptorChain(index + 1, processor);
                return interceptors.get(index).intercept(chain);
            } else {
                return processor;
            }
        }
    }

     */

    /* From OkHttp

    public interface Interceptor {
        Response intercept(Chain chain) throws IOException;

        interface Chain {
            Request request();
            Response proceed(Request request) throws IOException;
        }
    }

    private Response getResponseWithInterceptorChain(boolean forWebSocket) throws IOException {
        Interceptor.Chain chain = new ApplicationInterceptorChain(0, originalRequest, forWebSocket);
        return chain.proceed(originalRequest);
    }

    class ApplicationInterceptorChain implements Interceptor.Chain {
        private final int index;
        private final Request request;
        private final boolean forWebSocket;

        ApplicationInterceptorChain(int index, Request request, boolean forWebSocket) {
          this.index = index;
          this.request = request;
          this.forWebSocket = forWebSocket;
        }

        @Override public Connection connection() {
          return null;
        }

        @Override public Request request() {
          return request;
        }

        @Override public Response proceed(Request request) throws IOException {
          if (index < client.interceptors().size()) {
            // There's another interceptor in the chain. Call that.
            Interceptor.Chain chain = new ApplicationInterceptorChain(index + 1, request, forWebSocket);
            return client.interceptors().get(index).intercept(chain);
          } else {
            // No more interceptors. Do HTTP.
            return getResponse(request, forWebSocket);
          }
        }
    }

     */

    /*
    Need to use annotations

    @ProxyType(Source.class) or @ProxyType("Source")
    class SourceProxy<T, P extends Params> {
        @ProxyMethod("call")
        Action2<P, Subscriber<? super T>> source;

        @ProxyMethod("get")
        Func0<T> value;

        @ProxyTransform("call")
        Action2<P, Subscriber<? super T>> source(final Action1<Subscriber<? super T>> source) {
            return new Action2<P, Subscriber<? super T>>() {
                @Override public void call(P p, Subscriber<? super T> subscriber) {
                    source.call(subscriber);
                }
            };
        }

        @ProxyTransform("call")
        Action2<P, Subscriber<? super T>> source(final Func1<P, T> source) {
            return new Action2<P, Subscriber<? super T>>() {
                @Override public void call(P p, Subscriber<? super T> subscriber) {
                    try {
                        T t = source.call(p);
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(t);
                            subscriber.onCompleted();
                        }
                    } catch (Throwable e) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onError(e);
                        }
                    }
                }
            };
        }

        @ProxyTransform("get")
        Func0<T> value(final T t) {
            return new Func0<T>() {
                @Override public T call() {
                    return t;
                }
            };
        }
    }

    // Generated Interface
    interface Source<T, P extends Params> {
        void call(P p, Subscriber<? super T> subscriber);

        T get();
    }

    // Generated Invocation Handler
    final class SourceHandler<T, P extends Params> implements MethodHandler {
        private final Action2<P, Subscriber<? super T>> source;

        private SourceHandler(Action2<P, Subscriber<? super T>> source) {
            this.source = source;
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean handle(Object proxy, Method method, Object[] args, MethodResult result) throws Throwable {
            Class<?>[] paramTypes = method.getParameterTypes();
            if (paramTypes.length == 2
                    && paramTypes[0].isAssignableFrom(Params.class)
                    && paramTypes[1].isAssignableFrom(Subscriber.class)) {
                source.call((P) args[0], (Subscriber<? super T>) args[1]);
                return true;
            }
            return false;
        }
    }

    // Generated Builder
    final class SourceModule<T, P extends Params> implements TypeModule<Source> {
        final ProxySource<T, P> proxy = new ProxySource<T, P>();

        Builder<T, P> source(Action2<P, Subscriber<? super T>> source) {
            proxy.source = source;
            return this;
        }

        Builder<T, P> source(Action1<Subscriber<? super T>> source) {
            proxy.source = proxy.source(source);
            return this;
        }

        Builder<T, P> source(final Func1<P, T> source) {
            proxy.source = proxy.source(source);
            return this;
        }

        Builder<T, P> value(Func0<T> value) {
            proxy.value = value;
            return this;
        }

        Builder<T, P> value(T t) {
            proxy.value = proxy.value(t);
            return this;
        }

       @Override TypeHandler<Source> build() {
            if (proxy.source == null) {
                throw new IllegalStateException("source must be set");
            }
            return new TypeHandler.Builder<Source>(Source.class)
                    .handle("call", new SourceHandler<T, P>(proxy.source))
                    .build();
        }
    }
    presents ->

    @GenerateBuilder(OtherProxySource.class)
    public interface ExtendedSource<T, P extends Params> implements Source<T, P> {}
    */
}
