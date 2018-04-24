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

package com.laynemobile.proxy.cache;

import com.google.common.collect.ImmutableList;
import com.laynemobile.proxy.functions.Func0;
import com.laynemobile.proxy.functions.Func1;
import com.laynemobile.proxy.functions.Func2;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public interface ParameterizedCache<K, V, P> extends Cache<K, V> {
    // @Id("get")
    // @DependsOn(StateImpl)
    @Override V get(K k);

    // @DependsOn("get")
    V getOrCreate(K k, P p);

    // @DependsOn(StateImpl)
    @Override ImmutableList<V> values();

    interface Creator<K, V, P> {
        V create(K k, P p);
    }

    // @State
    interface StateImpl<K, V, P> {
        Map<K, V> cache();

        Creator<K, V, P> creator();
    }



    /*

    // existing type (implementation left out)
    type Map<K, V> {
        func V get(K k) { ... }

        func void put(K k, V v) { ... }

        func Collection<V> values() { ... }
    }

    type Cache<K, V, P> {
        // has no dependencies,
        type Creator<K, V, P> {
            func V create(K k, P p) {
                dependsOn -> (nada)
                default -> (nada)
            }
        }

        // state type is immutable, created once
        state {
            func Map<K, V> map() {
                dependsOn -> (nada)
                default -> {
                    return new HashMap<>()
                }
            }

            func Creator<K, V, P> creator() {
                dependsOn -> (nada)
                default -> (nada)
            }
        }

        func V get(K k) {
            dependsOn -> (State)
            default -> {
                Map<K, V> map = State.map()
                return map.get(k)
            }
        }

        func V getOrCreate(K k, P p) {
            dependsOn -> (func_get, State)
            default -> {
                V v = func_get(k)
                if (v == null) {
                    v = State.creator().create(k, p)
                    State.map().put(k, v)
                }
                return v
            }
        }

        func Collection<V> values() {
            dependsOn -> (State)
            default -> {
                return State.map().values()
            }
        }
    }

    type Logger {
        func void log(String msg, Object... args) {
            dependsOn -> (nada)
            default -> (nada)
        }
    }

    type LogCache<K, V, P> inherits Cache<K, V, P> {
        // Automatically inherits Cache state
        state {
            func Logger logger() {
                dependsOn -> (nada)
                default -> {
                    // empty default implementation
                    return new Logger() { ... }
                }
            }
        }

        // automatically inherits: ('inherit' type methods + state)
        override func V get(K k) {
            // unneeded dependsOn -> (...)
            default -> {
                V v = Cache.func_get(k)
                state.logger().log("cache value: %s from key: %s", v, k)
                return v
            }
        }
    }

    type EnvCache<K, V> inherits LogCache<K, V, Env> {
        state {
            override func Logger logger() {
                default -> {
                    return new Logger() {

                    }
                }
            }
        }
    }

     */

//    Func2<Func1<K, V>, Creator<K, V, P>, Func2<K, P, V>> getOrCreate();

    /*
        return new Func2<>() {
           @Override public Func2<K, P, V> call(final Func1<K, V> get, final Creator<K, V, P> creator) {
                return new Func2<>() {
                    @Override public Func2<K, P, V> call(K k, P p) {
                        V v = get.call(k);
                        if (v == null) {

                        }
                    }
                };
            }
        };
     */

    final class State<K, V, P> {
        final Map<K, V> cache;
        final Func2<K, P, V> create;

        State(Func2<K, P, V> create) {
            this(new HashMap<K, V>(), create);
        }

        State(Func0<Map<K, V>> cache, Func2<K, P, V> create) {
            this(cache.call(), create);
        }

        State(Map<K, V> cache, Func2<K, P, V> create) {
            this.cache = cache;
            this.create = create;
        }
    }

    final class CacheProxyImpl<K, V, P> {
        private final Func1<K, V> get; // depends on shared cache (unknown)
        private final Func2<K, P, V> getOrCreate; // depends on 'get' and creator
        private final Func0<Collection<V>> values; // depends on shared cache (unknown)

        private CacheProxyImpl(B2<K, V, P> b2) {
            this.get = b2.get;
            this.values = b2.values;
            this.getOrCreate = b2.getOrCreate;
        }
    }

    // TODO: State would have it's own builder like Cache?

    final class B1<K, V, P> {
        private final State<K, V, P> state;

        private Func1<K, V> get; // depends on shared state
        private Func0<Collection<V>> values; // depends on shared state

        private B1(State<K, V, P> state) {
            this.state = state;
        }

        static <K, V, P> B1<K, V, P> builder(Func2<K, P, V> create) {
            return new B1<>(new State<>(create));
        }

        B1<K, V, P> get(Func1<State<K, V, P>, Func1<K, V>> get) {
            this.get = get.call(state);
            return this;
        }

        B1<K, V, P> values(Func1<State<K, V, P>, Func0<Collection<V>>> values) {
            this.values = values.call(state);
            return this;
        }

        B2<K, V, P> next() {
            return new B2<>(this);
        }
    }

    final class B2<K, V, P> {
        final State<K, V, P> state;
        final Func1<K, V> get;
        final Func0<Collection<V>> values;

        Func2<K, P, V> getOrCreate; // depends on 'get' and shared state

        private B2(B1<K, V, P> b1) {
            // TODO: check not null
            this.state = b1.state;
            this.get = b1.get;
            this.values = b1.values;
        }

        B2<K, V, P> getOrCreate(Func2<Func1<K, V>, State<K, V, P>, Func2<K, P, V>> getOrCreate) {
            this.getOrCreate = getOrCreate.call(get, state);
            return this;
        }

        CacheProxyImpl<K, V, P> build() {
            return new CacheProxyImpl<>(this);
        }
    }

    class Playground<K, V, P> {
        private Playground(final Creator<K, V, P> creator) {
            Func2<K, P, V> create = new Func2<K, P, V>() {
                @Override public V call(K k, P p) {
                    // Creator passed in (dependency)
                    return creator.create(k, p);
                }
            };
            final CacheProxyImpl<K, V, P> cache = B1.builder(create)
                    .get(new Func1<State<K, V, P>, Func1<K, V>>() {
                        @Override public Func1<K, V> call(final State<K, V, P> state) {
                            return new Func1<K, V>() {
                                @Override public V call(K k) {
                                    synchronized (state.cache) {
                                        return state.cache.get(k);
                                    }
                                }
                            };
                        }
                    })
                    .values(new Func1<State<K, V, P>, Func0<Collection<V>>>() {
                        @Override public Func0<Collection<V>> call(final State<K, V, P> state) {
                            return new Func0<Collection<V>>() {
                                @Override public Collection<V> call() {
                                    synchronized (state.cache) {
                                        return new LinkedHashSet<>(state.cache.values());
                                    }
                                }
                            };
                        }
                    })
                    .next()
                    .getOrCreate(new Func2<Func1<K, V>, State<K, V, P>, Func2<K, P, V>>() {
                        @Override public Func2<K, P, V> call(final Func1<K, V> getFunc, final State<K, V, P> state) {
                            return new Func2<K, P, V>() {
                                @Override public V call(K k, P p) {
                                    V cached;
                                    if ((cached = getFunc.call(k)) == null) {
                                        V created = state.create.call(k, p);
                                        synchronized (state.cache) {
                                            if ((cached = getFunc.call(k)) == null) {
                                                state.cache.put(k, created);
                                                return created;
                                            }
                                        }
                                    }
                                    return cached;
                                }
                            };
                        }
                    })
                    .build();
        }
    }

    /*

    final class Impl<K, V, P> {
        private final Map<K, V> cache = new HashMap<>();
        private final Creator<K, V, P> creator;

        private Impl(Creator<K, V, P> creator) {
            this.creator = creator;
        }

        private V get(K k) {
            synchronized (cache) {
                return cache.get(k);
            }
        }

        private V getOrCreate(K k, P p) {
            V cached;
            if ((cached = get(k)) == null) {
                V created = creator.create(k, p);
                synchronized (cache) {
                    if ((cached = get(k)) == null) {
                        cache.put(k, created);
                        return created;
                    }
                }
            }
            return cached;
        }

        private Collection<V> values() {
            synchronized (cache) {
                return new LinkedHashSet<>(cache.values());
            }
        }
    }
     */
}
