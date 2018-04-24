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

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.laynemobile.proxy.Util.Transformer;
import com.laynemobile.proxy.annotations.GenerateProxyHandlerFunction;
import com.laynemobile.proxy.cache.EnvCache;
import com.laynemobile.proxy.cache.MultiAliasCache;
import com.laynemobile.proxy.elements.AliasElements;
import com.laynemobile.proxy.elements.ExecutableElementAlias;
import com.laynemobile.proxy.elements.TypeElementAlias;
import com.laynemobile.proxy.model.output.ProxyFunctionAbstractTypeOutputStub;
import com.laynemobile.proxy.model.output.TypeElementGenerator;
import com.laynemobile.proxy.types.AliasTypes;
import com.laynemobile.proxy.types.DeclaredTypeAlias;
import com.laynemobile.proxy.types.TypeMirrorAlias;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import sourcerer.processor.Env;

import static com.laynemobile.proxy.Util.buildList;

public class ProxyFunctionElement extends AbstractValueAlias<MethodElement> implements TypeElementGenerator {
    private static MultiAliasCache<TypeElementAlias, MethodElement, ProxyFunctionElement> CACHE
            = MultiAliasCache.create(new Creator());

    private final String name;
    private final ImmutableSet<ProxyFunctionElement> overrides;
    private final TypeElementAlias functionElement;
    private final DeclaredTypeAlias functionType;
    private final TypeElementAlias abstractProxyFunctionElement;
    private final DeclaredTypeAlias abstractProxyFunctionType;
    private final ImmutableList<TypeMirror> boxedParamTypes;
    private final AtomicReference<ProxyFunctionAbstractTypeOutputStub> output = new AtomicReference<>();

    private ProxyFunctionElement(MethodElement source, Set<? extends ProxyFunctionElement> overrides, Env env) {
        super(source);
        ExecutableElement element = source.element();
        GenerateProxyHandlerFunction function = element.getAnnotation(GenerateProxyHandlerFunction.class);
        String name = function == null ? "" : function.value();
        if (name.isEmpty()) {
            name = element.getSimpleName().toString();
        }
        env.log("name: %s", name);

        List<TypeMirrorAlias> params = source.paramTypes();
        int length = params.size();
        String num;
        if (length > 9) {
            length = 0;
            num = "N";
        } else {
            num = Integer.toString(length);
        }

        TypeMirrorAlias returnType = source.returnType();
        String functionClass;
        TypeMirror[] paramTypes;
        env.log("returnType: %s", returnType);
        if (returnType.getKind() == TypeKind.VOID) {
            env.log("void");
            paramTypes = new TypeMirror[length];
            functionClass = "Action";
        } else {
            env.log("not void");
            paramTypes = new TypeMirror[length + 1];
            paramTypes[length] = boxedType(returnType, env);
            functionClass = "Func";
        }

        env.log("params length: %d", length);
        env.log("params: %s", params);
        ImmutableList.Builder<TypeMirror> boxedParamTypes = ImmutableList.builder();
        for (int i = 0; i < length; i++) {
            TypeMirror boxedType = boxedType(params.get(i), env);
            paramTypes[i] = boxedType;
            boxedParamTypes.add(boxedType);
        }
        env.log("paramTypes: %s", Arrays.toString(paramTypes));

        Elements elementUtils = env.elements();
        Types typeUtils = env.types();
        String packageName = "com.laynemobile.proxy.functions.";
        TypeElementAlias functionElement = AliasElements.get(
                elementUtils.getTypeElement(packageName + functionClass + num));
        env.log("function element: %s", functionElement);
        DeclaredTypeAlias functionType = AliasTypes.get(
                typeUtils.getDeclaredType(functionElement.actual(), paramTypes));
        env.log("function type: %s", functionType);

        TypeElement abstractProxyFunctionElement
                = elementUtils.getTypeElement(packageName + "AbstractProxyFunction");
        DeclaredTypeAlias abstractProxyFunctionType
                = AliasTypes.get(typeUtils.getDeclaredType(abstractProxyFunctionElement, functionType.actual()));
        env.log("AbstractProxyFunction type: %s", abstractProxyFunctionType);
        env.log("AbstractProxyFunction type typeArguments: %s", abstractProxyFunctionType.getTypeArguments());

        this.name = name;
        this.overrides = ImmutableSet.copyOf(overrides);
        this.functionElement = AliasElements.get(functionElement);
        this.functionType = functionType;
        this.abstractProxyFunctionElement = AliasElements.get(abstractProxyFunctionElement);
        this.abstractProxyFunctionType = abstractProxyFunctionType;
        this.boxedParamTypes = boxedParamTypes.build();
    }

    public static ImmutableList<ProxyFunctionElement> parse(TypeElementAlias typeElement, final Env env) {
        return transform(typeElement, MethodElement.parse(typeElement, env), env);
    }

    public static ImmutableSet<ProxyFunctionElement> inherited(TypeElementAlias typeElement,
            Collection<? extends TypeElementAlias> superElements, Env env) {
        Set<MethodElement> methods = MethodElement.inherited(typeElement, superElements, env);
        return ImmutableSet.copyOf(transform(typeElement, methods, env));
    }

    public static ImmutableSet<ProxyFunctionElement> inherited(TypeElementAlias typeElement,
            TypeElementAlias superElement, Env env) {
        Set<MethodElement> methods = MethodElement.inherited(typeElement, superElement, env);
        return ImmutableSet.copyOf(transform(typeElement, methods, env));
    }

    private static ImmutableList<ProxyFunctionElement> transform(TypeElementAlias typeElement,
            Collection<? extends MethodElement> methods, final Env env) {
        final EnvCache<MethodElement, ProxyFunctionElement> cache = CACHE.getOrCreate(typeElement, env);
        return buildList(methods, new Transformer<ProxyFunctionElement, MethodElement>() {
            @Override public ProxyFunctionElement transform(MethodElement element) {
                return cache.getOrCreate(element, env);
            }
        });
    }

    private static TypeMirror boxedType(TypeMirrorAlias typeMirror, Env env) {
        Types typeUtils = env.types();
        if (typeMirror.getKind().isPrimitive()) {
            return typeUtils.boxedClass((PrimitiveType) typeMirror.actual())
                    .asType();
        }
        return typeMirror.actual();
    }

    public ProxyElement parent() {
        TypeElementAlias typeElement = typeElement();
        ProxyElement parent = ProxyElement.cache().get(typeElement);
        if (parent == null) {
            throw new IllegalStateException(typeElement + " parent must be in cache");
        }
        return parent;
    }

    public MethodElement alias() {
        return value();
    }

    public TypeElementAlias typeElement() {
        return value().typeElement();
    }

    public ExecutableElementAlias element() {
        return value().element();
    }

    public ImmutableSet<ProxyFunctionElement> overrides() {
        return overrides;
    }

    public TypeElementAlias abstractProxyFunctionElement() {
        return abstractProxyFunctionElement;
    }

    public DeclaredTypeAlias abstractProxyFunctionType() {
        return abstractProxyFunctionType;
    }

    public ImmutableList<TypeMirror> boxedParamTypes() {
        return boxedParamTypes;
    }

    public TypeElementAlias functionElement() {
        return functionElement;
    }

    public DeclaredTypeAlias functionType() {
        return functionType;
    }

    public String name() {
        return name;
    }

    public AtomicReference<ProxyFunctionAbstractTypeOutputStub> output() {
        return output;
    }

    @Override public ProxyFunctionAbstractTypeOutputStub outputStub() {
        ProxyFunctionAbstractTypeOutputStub o;
        AtomicReference<ProxyFunctionAbstractTypeOutputStub> ref = output;
        if ((o = ref.get()) == null) {
            ref.compareAndSet(null, newOutput());
            return ref.get();
        }
        return o;
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("element", element())
                .toString();
    }

    @Override public String toDebugString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("\nelement", element())
                .add("\noverrides", overrides)
                .add("\nfunctionElement", functionElement)
                .add("\nabstractProxyFunctionElement", abstractProxyFunctionElement)
                .add("\nfunctionType", functionType)
                .add("\nabstractProxyFunctionType", abstractProxyFunctionType)
                .toString();
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProxyFunctionElement)) return false;
        if (!super.equals(o)) return false;
        ProxyFunctionElement that = (ProxyFunctionElement) o;
        return Objects.equal(name, that.name) &&
                Objects.equal(functionType, that.functionType);
    }

    @Override public int hashCode() {
        return Objects.hashCode(super.hashCode(), name, functionType);
    }

    private static final class Creator implements MultiAliasCache.ValueCreator<TypeElementAlias, MethodElement, ProxyFunctionElement> {
        @Override public ProxyFunctionElement create(TypeElementAlias typeElement, MethodElement element, Env env) {
            ImmutableSet.Builder<ProxyFunctionElement> overrides = ImmutableSet.builder();
            ProxyFunctionElement _overrides = overrides(typeElement.getSuperclass(), element, env);
            if (_overrides != null) {
                overrides.add(_overrides);
            }
            for (TypeMirrorAlias typeAlias : typeElement.getInterfaces()) {
                if ((_overrides = overrides((DeclaredTypeAlias) typeAlias, element, env)) != null) {
                    overrides.add(_overrides);
                }
            }
            ProxyFunctionElement proxyFunctionElement = new ProxyFunctionElement(element, overrides.build(), env);
            env.log("created proxy function element: %s\n\n", proxyFunctionElement.toDebugString());
            return proxyFunctionElement;
        }

        private ProxyFunctionElement overrides(TypeMirrorAlias typeAlias, MethodElement element, Env env) {
            if (typeAlias != null && typeAlias.getKind() == TypeKind.DECLARED) {
                return overrides((DeclaredTypeAlias) typeAlias, element, env);
            }
            return null;
        }

        private ProxyFunctionElement overrides(DeclaredTypeAlias typeAlias, MethodElement element, Env env) {
            if (typeAlias != null) {
                TypeElementAlias tea = (TypeElementAlias) typeAlias.asElement();
                for (MethodElement methodElement : MethodElement.parse(tea, env)) {
                    if (element.overrides(methodElement, env)) {
                        return CACHE.getOrCreate(tea, methodElement, env);
                    }
                }
            }
            return null;
        }
    }

    private ProxyFunctionAbstractTypeOutputStub newOutput() {
        return ProxyFunctionAbstractTypeOutputStub.create(this);
    }
}
