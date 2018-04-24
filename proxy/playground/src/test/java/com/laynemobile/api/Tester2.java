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
import com.laynemobile.proxy.ProxyBuilder2;
import com.laynemobile.proxy.ProxyObject2;
import com.laynemobile.proxy.ProxyType2;
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class Tester2 {
    private static final String TAG = Tester2.class.getSimpleName();

    static {
        ProxyLog.setLogger(new ConsoleLogger());
    }

    @Test public void testSourceProxy() throws Throwable {
        Source<Potato, PotatoParams> source = new Source2ProxyTypeBuilder<Potato, PotatoParams>()
                .setSource(new Func1<PotatoParams, Potato>() {
                    @Override public Potato call(PotatoParams params) {
                        return new Potato(params.kind());
                    }
                })
                .build()
                .castToType();

        ProxyObject2<Source<Potato, PotatoParams>> sourceProxyObject = assertProxyObject(source, Source.class);

        final PotatoParams params = new DefaultPotatoParams("russet");
        assertPotatoSource(source, params);
    }

    private static <T> ProxyObject2<T> assertProxyObject(Object proxy, Class<?>... expectedTypes) {
        assertTrue("object " + proxy + "not instance of ProxyObject2", proxy instanceof ProxyObject2);
        @SuppressWarnings("unchecked")
        ProxyObject2<T> proxyObject = (ProxyObject2<T>) proxy;
        ProxyLog.d(TAG, "runTest proxyObject: %s", proxyObject);
        ProxyLog.d(TAG, "proxyObject type() -> ", proxyObject.type());

        ProxyBuilder2<T> proxyBuilder = proxyObject.newProxyBuilder();
        assertNotNull(proxyBuilder);

        SortedSet<ProxyType2<? extends T>> proxyTypes = proxyObject.proxyTypes();
        FOUND:
        for (Class<?> expectedType : expectedTypes) {
            assertTrue("object " + proxy + "not instance of expected type: " + expectedType,
                    expectedType.isInstance(proxy));
            for (ProxyType2<?> proxyType : proxyTypes) {
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
