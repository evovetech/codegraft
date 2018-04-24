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

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.laynemobile.proxy.ProxyBuilder;
import com.laynemobile.proxy.ProxyObject;
import com.laynemobile.proxy.ProxyType;
import com.laynemobile.proxy.TypeToken;
import com.laynemobile.proxy.functions.Func0;
import com.laynemobile.proxy.functions.Func1;
import com.laynemobile.proxy.internal.ConsoleLogger;
import com.laynemobile.proxy.internal.ProxyLog;

import org.junit.Test;

import java.util.SortedSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import rx.Subscriber;

import static java.lang.String.format;
import static java.util.Locale.US;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class Tester {
    private static final String TAG = Tester.class.getSimpleName();

    static {
        ProxyLog.setLogger(new ConsoleLogger());
    }

    @Test public void testSourceProxy() throws Throwable {
        final NetworkChecker networkChecker = new SimpleNetworkChecker();
        Source<Potato, PotatoParams> source = new SourceProxyTypeBuilder<Potato, PotatoParams>()
                .setSource(new Func1<PotatoParams, Potato>() {
                    @Override public Potato call(PotatoParams params) {
                        return new Potato(params.kind());
                    }
                })
                .build()
                .castToType();

        ProxyObject<Source<Potato, PotatoParams>> sourceProxyObject = assertProxyObject(source, Source.class);

        final PotatoParams params = new DefaultPotatoParams("russet");
        assertPotatoSource(source, params);
        // assert source is not instance of network source
        assertFalse(source instanceof NetworkSource);

        // Now add network handler
        final ProxyType<NetworkSource<Potato, PotatoParams>> networkHandler;
        networkHandler = new NetworkSourceProxyTypeBuilder<Potato, PotatoParams>()
                .setNetworkChecker(networkChecker)
                .buildProxyType();
        // Adding network handler, allows for NetworkSource addition
        source = sourceProxyObject.newProxyBuilder()
                .add(networkHandler)
                .build()
                .castToType();

        assertProxyObject(source, Source.class, NetworkSource.class);

        // Note: able to cast by adding NetworkSource proxy handler
        NetworkSource<Potato, PotatoParams> networkSource = (NetworkSource<Potato, PotatoParams>) source;
        assertEquals(networkChecker, networkSource.networkChecker());
        assertPotatoSource(networkSource, params);

        ProxyLog.d(TAG, "networkSource.toString() -> %s", networkSource.toString());
    }

//    @Test public void testRetrofitSourceProxy() throws Throwable {
//        final DefaultPotatoFactory potatoFactory = new DefaultPotatoFactory();
//        final NetworkChecker networkChecker = new SimpleNetworkChecker();
//
//        RetrofitSource<Potato, PotatoParams, PotatoFactory> source = new RetrofitSourceProxyHandlerBuilder3<Potato, PotatoParams, PotatoFactory>()
//                .setRetrofittable(new Func0<Retrofittable<PotatoFactory>>() {
//                    @Override public Retrofittable<PotatoFactory> call() {
//                        return potatoFactory;
//                    }
//                })
//                .setSource(new Func2<PotatoFactory, PotatoParams, Potato>() {
//                    @Override public Potato call(PotatoFactory potatoFactory, PotatoParams params) {
//                        return potatoFactory.newPotato(params);
//                    }
//                })
//                .setNetworkChecker(new Func0<NetworkChecker>() {
//                    @Override public NetworkChecker call() {
//                        return networkChecker;
//                    }
//                })
//                .build();
//
//        final PotatoParams params = new DefaultPotatoParams("russet");
//        assertPotatoSource(source, params);
//        assertEquals(potatoFactory, source.getRetrofittable());
//        assertEquals(networkChecker, source.networkChecker());
//    }

    @Test public void testSimpleSourceProxy() throws Throwable {
        final TypeToken<Source<Potato, NoParams>> type = new TypeToken<Source<Potato, NoParams>>() {};
        final Potato potato = new Potato("russet");
        final NetworkChecker networkChecker = new SimpleNetworkChecker();

        // Create simple source handler
        ProxyType<SimpleSource<Potato>> sourceHandler = new SimpleSourceProxyTypeBuilder<Potato>()
                .setSource(new Func0<Potato>() {
                    @Override public Potato call() {
                        return potato;
                    }
                })
                .buildProxyType();

        // Create network source handler
        ProxyType<NetworkSource<Potato, NoParams>> networkSourceHandler = new NetworkSourceProxyTypeBuilder<Potato, NoParams>()
                .setNetworkChecker(networkChecker)
                .buildProxyType();

        // Create the proxy
        Source<Potato, NoParams> _source = new ProxyBuilder<>(type)
                .add(sourceHandler)
                .add(networkSourceHandler)
                .build()
                .castToType();

        assertProxyObject(_source, Source.class, SimpleSource.class, NetworkSource.class);

        // Note: able to cast by adding SimpleSource proxy handler
        SimpleSource<Potato> simpleSource = (SimpleSource<Potato>) _source;
        assertSimpleSource(simpleSource, potato);

        // Note: able to cast by adding NetworkSource proxy handler
        NetworkSource<Potato, NoParams> networkSource = (NetworkSource<Potato, NoParams>) _source;
        assertEquals(networkChecker, networkSource.networkChecker());
    }

    private static <T> ProxyObject<T> assertProxyObject(Object proxy, Class<?>... expectedTypes) {
        assertTrue("object " + proxy + "not instance of ProxyObject", proxy instanceof ProxyObject);
        @SuppressWarnings("unchecked")
        ProxyObject<T> proxyObject = (ProxyObject<T>) proxy;
        ProxyLog.d(TAG, "runTest proxyObject: %s", proxyObject);
        ProxyLog.d(TAG, "proxyObject type() -> ", proxyObject.type());

        ProxyBuilder<T> proxyBuilder = proxyObject.newProxyBuilder();
        assertNotNull(proxyBuilder);

        SortedSet<ProxyType<? extends T>> proxyTypes = proxyObject.proxyTypes();
        FOUND:
        for (Class<?> expectedType : expectedTypes) {
            assertTrue("object " + proxy + "not instance of expected type: " + expectedType,
                    expectedType.isInstance(proxy));
            for (ProxyType<?> proxyType : proxyTypes) {
                if (proxyType.rawTypes().contains(expectedType)) {
                    continue FOUND;
                }
            }
            fail(format(US, "didn't find expected ProxyType '%s' in ProxyObject: '%s'", expectedType, proxyObject));
        }
        return proxyObject;
    }

    private static <T, P extends Params> void assertSource(Source<T, P> source, P params, T expected) throws Throwable {
        final AtomicReference<T> onNext = new AtomicReference<>();
        final AtomicBoolean onCompleted = new AtomicBoolean();
        final AtomicReference<Throwable> onError = new AtomicReference<>();

        final CountDownLatch latch = new CountDownLatch(1);
        source.call(params, new Subscriber<T>() {
            @Override public void onNext(T t) {
                onNext.set(t);
                latch.countDown();
            }

            @Override public void onCompleted() {
                onCompleted.set(true);
                latch.countDown();
            }

            @Override public void onError(Throwable e) {
                onError.set(e);
                latch.countDown();
            }
        });
        latch.await();

        Throwable e = onError.get();
        if (e != null) {
            throw e;
        }

        assertEquals(expected, onNext.get());
        assertTrue(onCompleted.get());
        assertNull(onError.get());
    }

    private static <T> void assertSimpleSource(SimpleSource<T> source, T expected) throws Throwable {
        assertSource(source, NoParams.instance(), expected);
    }

    private static void assertPotatoSource(Source<Potato, PotatoParams> source, PotatoParams params) throws Throwable {
        assertSource(source, params, new Potato(params.kind()));
    }

    private static final class Potato {
        private final String kind;

        private Potato(String kind) {
            this.kind = kind;
        }

        private String kind() {
            return kind;
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Potato)) return false;
            Potato potato = (Potato) o;
            return Objects.equal(kind, potato.kind);
        }

        @Override public int hashCode() {
            return Objects.hashCode(kind);
        }
    }

    private interface PotatoParams extends Params {
        String kind();
    }

    private static final class DefaultPotatoParams implements PotatoParams {
        private final String kind;

        private DefaultPotatoParams(String kind) {
            this.kind = kind;
        }

        @Override public String kind() {
            return kind;
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DefaultPotatoParams)) return false;
            DefaultPotatoParams that = (DefaultPotatoParams) o;
            return Objects.equal(kind, that.kind);
        }

        @Override public int hashCode() {
            return Objects.hashCode(kind);
        }
    }

    private interface PotatoFactory {
        Potato newPotato(PotatoParams params);
    }

    private static final class DefaultPotatoFactory implements PotatoFactory, Retrofittable<PotatoFactory> {
        @Override public Potato newPotato(PotatoParams params) {
            return new Potato(params.kind());
        }

        @Override public PotatoFactory getService() {
            return this;
        }
    }

    private static final class SimpleNetworkChecker implements NetworkChecker {
        final boolean networkAvailable;

        SimpleNetworkChecker() {
            this(true);
        }

        SimpleNetworkChecker(boolean networkAvailable) {
            this.networkAvailable = networkAvailable;
        }

        @Override public boolean isNetworkAvailable() {
            return networkAvailable;
        }

        @Override public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("networkAvailable", networkAvailable)
                    .toString();
        }
    }
}
