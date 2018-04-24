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

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.laynemobile.proxy.Util.Transformer;
import com.laynemobile.proxy.annotations.GenerateProxyHandler;
import com.laynemobile.proxy.cache.ParameterizedCache;
import com.laynemobile.proxy.elements.TypeElementAlias;
import com.laynemobile.proxy.output.ProxyHandlerBuilderOutputStub;
import com.squareup.javapoet.ClassName;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;

import sourcerer.processor.Env;

import static com.laynemobile.proxy.Util.buildSet;

public final class AnnotatedProxyElement extends AbstractValueAlias<ProxyElement> {
    private final Env env;
    private final GenerateProxyHandler annotation;
    private final ImmutableSet<AnnotatedProxyType> annotatedDirectDependencies;
    private final ImmutableSet<ProxyType> unannotatedDirectDependencies;
    private final ImmutableSet<AnnotatedProxyType> annotatedParamDependencies;
    private final ImmutableSet<ProxyType> unannotatedParamDependencies;
    private final ImmutableSet<AnnotatedProxyElement> annotatedOverrides;
    private final ImmutableSet<ProxyElement> unannotatedOverrides;
    private final ImmutableSet<ProxyFunctionElement> functions;

    private AnnotatedProxyElement(ProxyElement element, GenerateProxyHandler annotation, Env env) {
        super(element);
        Set<ProxyElement> overrides = new HashSet<>(element.overrides());
        for (ProxyType dependency : unannotatedTypes(element.allDependencies(), env)) {
            ProxyElement dependencyElement = dependency.element();
            if (dependencyElement.element().getKind() == ElementKind.INTERFACE) {
                overrides.add(dependencyElement);
            }
        }
        ImmutableSet<ProxyElement> unannotatedOverrides = unannotatedElements(overrides, env);
        ImmutableSet<TypeElementAlias> unannotatedOverrideAliases
                = buildSet(unannotatedOverrides, new Transformer<TypeElementAlias, ProxyElement>() {
            @Override public TypeElementAlias transform(ProxyElement proxyElement) {
                return proxyElement.element();
            }
        });

        this.env = env;
        this.annotation = annotation;
        this.annotatedDirectDependencies = annotatedTypes(element.directDependencies(), env);
        this.unannotatedDirectDependencies = unannotatedTypes(element.directDependencies(), env);
        this.annotatedParamDependencies = annotatedTypes(element.paramDependencies(), env);
        this.unannotatedParamDependencies = unannotatedTypes(element.paramDependencies(), env);
        this.annotatedOverrides = annotatedElements(overrides, env);
        this.unannotatedOverrides = unannotatedOverrides;
        this.functions = ImmutableSet.<ProxyFunctionElement>builder()
                .addAll(element.functions())
                .addAll(ProxyFunctionElement.inherited(element.element(), unannotatedOverrideAliases, env))
                .build();
        env.log("%s -- functions: %s", element, functions);
    }

    public static ParameterizedCache<ProxyElement, AnnotatedProxyElement, Env> cache() {
        return ElementCache.INSTANCE;
    }

    public static ImmutableSet<AnnotatedProxyElement> process(final Env env, RoundEnvironment roundEnv)
            throws IOException {
        try {
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(GenerateProxyHandler.class);
            return buildSet(elements, new Transformer<AnnotatedProxyElement, Element>() {
                @Override public AnnotatedProxyElement transform(Element element) {
                    // Ensure it is an interface element
                    if (element.getKind() != ElementKind.INTERFACE) {
                        env.error(element, "Only interfaces can be annotated with @%s",
                                GenerateProxyHandler.class.getSimpleName());
                        throw new RuntimeException("error");
                    }
                    return ElementCache.INSTANCE.parse(element, env);
                }
            });
        } catch (Throwable e) {
            throw new IOException(e);
        }
    }

    public ProxyElement element() {
        return value();
    }

    public GenerateProxyHandler annotation() {
        return annotation;
    }

    public String packageName() {
        return element().packageName();
    }

    public ClassName className() {
        return element().className();
    }

    public ImmutableSet<AnnotatedProxyType> annotatedDirectDependencies() {
        return annotatedDirectDependencies;
    }

    public ImmutableSet<ProxyType> unannotatedDirectDependencies() {
        return unannotatedDirectDependencies;
    }

    public ImmutableSet<AnnotatedProxyType> annotatedParamDependencies() {
        return annotatedParamDependencies;
    }

    public ImmutableSet<ProxyType> unannotatedParamDependencies() {
        return unannotatedParamDependencies;
    }

    public ImmutableSet<AnnotatedProxyElement> annotatedOverrides() {
        return annotatedOverrides;
    }

    public ImmutableSet<ProxyElement> unannotatedOverrides() {
        return unannotatedOverrides;
    }

    public ImmutableSet<AnnotatedProxyType> allAnnotatedDependencies() {
        return annotatedTypes(element().allDependencies(), env);
    }

    public ImmutableSet<ProxyType> allUnannotatedDependencies() {
        return unannotatedTypes(element().allDependencies(), env);
    }

    public ImmutableSet<ProxyFunctionElement> functions() {
        return functions;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AnnotatedProxyElement)) return false;
        return super.equals(o);
    }

    @Override public int hashCode() {
        return super.hashCode();
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("element", element())
                .toString();
    }

    private static ImmutableSet<AnnotatedProxyType> annotatedTypes(Set<? extends ProxyType> proxyTypes, final Env env) {
        return buildSet(proxyTypes, new Transformer<AnnotatedProxyType, ProxyType>() {
            @Override public AnnotatedProxyType transform(ProxyType proxyType) {
                return AnnotatedProxyType.cache().getOrCreate(proxyType, env);
            }
        });
    }

    private static ImmutableSet<ProxyType> unannotatedTypes(Set<? extends ProxyType> proxyTypes, final Env env) {
        return buildSet(proxyTypes, new Transformer<ProxyType, ProxyType>() {
            @Override public ProxyType transform(ProxyType proxyType) {
                if (AnnotatedProxyType.cache().getOrCreate(proxyType, env) == null) {
                    return proxyType;
                }
                return null;
            }
        });
    }

    private static ImmutableSet<AnnotatedProxyElement> annotatedElements(Set<? extends ProxyElement> proxyElements,
            final Env env) {
        return buildSet(proxyElements, new Transformer<AnnotatedProxyElement, ProxyElement>() {
            @Override public AnnotatedProxyElement transform(ProxyElement proxyElement) {
                return cache().getOrCreate(proxyElement, env);
            }
        });
    }

    private static ImmutableSet<ProxyElement> unannotatedElements(Set<? extends ProxyElement> proxyElements,
            final Env env) {
        return buildSet(proxyElements, new Transformer<ProxyElement, ProxyElement>() {
            @Override public ProxyElement transform(ProxyElement proxyElement) {
                if (cache().getOrCreate(proxyElement, env) == null) {
                    return proxyElement;
                }
                return null;
            }
        });
    }

    private static final class ElementCache implements ParameterizedCache<ProxyElement, AnnotatedProxyElement, Env> {
        private static final ElementCache INSTANCE = new ElementCache();

        private final Map<ProxyElement, AnnotatedProxyElement> cache = new HashMap<>();

        private ElementCache() {}

        @Override public AnnotatedProxyElement getOrCreate(ProxyElement proxyElement, Env env) {
            Result<AnnotatedProxyElement> cached = new Result<>();
            if (!getIfPresent(proxyElement, cached)) {
                AnnotatedProxyElement created = create(proxyElement, env);
                synchronized (cache) {
                    if (!getIfPresent(proxyElement, cached)) {
                        cache.put(proxyElement, created);
                        return created;
                    }
                }
            }
            return cached.get();
        }

        @Override public AnnotatedProxyElement get(ProxyElement proxyElement) {
            return getIfPresent(proxyElement);
        }

        private AnnotatedProxyElement parse(Element element, Env env) {
            // Ensure it is an interface element
            if (element.getKind() == ElementKind.INTERFACE) {
                ProxyElement proxyElement = ProxyElement.cache()
                        .parse(element, env);
                if (proxyElement != null) {
                    return getOrCreate(proxyElement, env);
                }
            }
            return null;
        }

        private AnnotatedProxyElement create(ProxyElement proxyElement, Env env) {
            if (proxyElement != null) {
                GenerateProxyHandler annotation;
                if ((annotation = proxyElement.element().getAnnotation(GenerateProxyHandler.class)) != null) {
                    return new AnnotatedProxyElement(proxyElement, annotation, env);
                } else if (ProxyHandlerBuilderOutputStub.exists(proxyElement, env)) {
                    env.log("returning existing AnnotatedProxyElement: %s", proxyElement);
                    return new AnnotatedProxyElement(proxyElement, null, env);
                }
            }
            return null;
        }

        private AnnotatedProxyElement getIfPresent(ProxyElement element) {
            Result<AnnotatedProxyElement> result = new Result<>();
            getIfPresent(element, result);
            return result.get();
        }

        private boolean getIfPresent(ProxyElement proxyElement, Result<AnnotatedProxyElement> out) {
            synchronized (cache) {
                if (cache.containsKey(proxyElement)) {
                    out.set(cache.get(proxyElement));
                    return true;
                }
                return false;
            }
        }

        @Override public ImmutableList<AnnotatedProxyElement> values() {
            synchronized (cache) {
                return ImmutableList.copyOf(cache.values());
            }
        }
    }
}
