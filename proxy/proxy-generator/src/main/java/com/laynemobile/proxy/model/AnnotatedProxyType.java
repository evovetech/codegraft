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

package com.laynemobile.proxy.model;

import com.google.common.collect.ImmutableList;
import com.laynemobile.proxy.cache.ParameterizedCache;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import sourcerer.processor.Env;

public class AnnotatedProxyType extends AbstractValueAlias<ProxyType> {
    private final AnnotatedProxyElement element;

    private AnnotatedProxyType(ProxyType proxyType, AnnotatedProxyElement element) {
        super(proxyType);
        this.element = element;
    }

    public static ParameterizedCache<ProxyType, AnnotatedProxyType, Env> cache() {
        return TypeCache.INSTANCE;
    }

    public AnnotatedProxyElement element() {
        return element;
    }

    private static final class TypeCache implements ParameterizedCache<ProxyType, AnnotatedProxyType, Env> {
        private static final TypeCache INSTANCE = new TypeCache();

        private final Map<ProxyType, AnnotatedProxyType> cache = new HashMap<>();

        private TypeCache() {}

        @Override public AnnotatedProxyType getOrCreate(ProxyType proxyType, Env env) {
            Result<AnnotatedProxyType> cached = new Result<>();
            if (!getIfPresent(proxyType, cached)) {
                AnnotatedProxyType created = create(proxyType, env);
                synchronized (cache) {
                    if (!getIfPresent(proxyType, cached)) {
                        cache.put(proxyType, created);
                        return created;
                    }
                }
            }
            return cached.get();
        }

        @Override public AnnotatedProxyType get(ProxyType proxyType) {
            return getIfPresent(proxyType);
        }

        private AnnotatedProxyType parse(TypeMirror type, Env env) {
            // Ensure it is an interface element
            if (type.getKind() == TypeKind.DECLARED) {
                ProxyType proxyType = ProxyType.cache()
                        .parse(type, env);
                if (proxyType != null) {
                    return get(proxyType);
                }
            }
            return null;
        }

        private AnnotatedProxyType create(ProxyType proxyType, Env env) {
            ProxyElement proxyElement = proxyType.element();
            AnnotatedProxyElement annotatedElement = AnnotatedProxyElement.cache()
                    .getOrCreate(proxyElement, env);
            if (annotatedElement != null) {
                return new AnnotatedProxyType(proxyType, annotatedElement);
            }
            return null;
        }

        private AnnotatedProxyType getIfPresent(ProxyType type) {
            Result<AnnotatedProxyType> result = new Result<>();
            getIfPresent(type, result);
            return result.get();
        }

        private boolean getIfPresent(ProxyType type, Result<AnnotatedProxyType> out) {
            synchronized (cache) {
                if (cache.containsKey(type)) {
                    out.set(cache.get(type));
                    return true;
                }
                return false;
            }
        }

        @Override public ImmutableList<AnnotatedProxyType> values() {
            synchronized (cache) {
                return ImmutableList.copyOf(cache.values());
            }
        }
    }
}
