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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.laynemobile.proxy.elements.AliasElements;
import com.laynemobile.proxy.elements.TypeElementAlias;
import com.laynemobile.proxy.elements.TypeParameterElementAlias;
import com.laynemobile.proxy.functions.Func0;
import com.laynemobile.proxy.internal.ProxyLog;
import com.laynemobile.proxy.types.TypeMirrorAlias;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;
import com.squareup.javapoet.WildcardTypeName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleTypeVisitor7;
import javax.lang.model.util.Types;

import sourcerer.processor.Env;

import static com.laynemobile.proxy.Constants.Builder;
import static com.laynemobile.proxy.Constants.List;
import static com.laynemobile.proxy.Constants.PACKAGE;

public final class Util {
    private static final String TAG = Util.class.getSimpleName();
    private static final ArrayCreator<TypeMirror> TYPE_MIRROR_ARRAY_CREATOR = new ArrayCreator<TypeMirror>() {
        @Override public TypeMirror[] newArray(int size) {
            return new TypeMirror[size];
        }
    };
    private static final ArrayCreator<TypeName> TYPE_NAME_ARRAY_CREATOR = new ArrayCreator<TypeName>() {
        @Override public TypeName[] newArray(int size) {
            return new TypeName[size];
        }
    };

    public static TypeMirror[] typeParameterArray(List<? extends TypeParameterElementAlias> typeParameters,
            final Env env, ArrayCreator<TypeMirror> arrayCreator) {
        return toArray(typeParameters(typeParameters, env), arrayCreator);
    }

    public static ImmutableList<? extends TypeMirror> typeParameters(
            List<? extends TypeParameterElementAlias> typeParameters, final Env env) {
        return buildList(typeParameters, new Transformer<TypeMirror, TypeParameterElementAlias>() {
            @Override public TypeMirror transform(TypeParameterElementAlias typeParameter) {
                return boxedType(typeParameter.asType(), env);
            }
        });
    }

    public static TypeMirror[] typeMirrorArray(List<? extends TypeMirror> list) {
        return toArray(list, TYPE_MIRROR_ARRAY_CREATOR);
    }

    public static TypeName[] typeNameArray(List<? extends TypeName> list) {
        return toArray(list, TYPE_NAME_ARRAY_CREATOR);
    }

    public static <T> T[] toArray(List<? extends T> list, ArrayCreator<T> arrayCreator) {
        if (list == null || list.isEmpty()) {
            return arrayCreator.newArray(0);
        }
        return list.toArray(arrayCreator.newArray(list.size()));
    }

    public static TypeMirror boxedType(TypeMirrorAlias typeMirror, Env env) {
        Types typeUtils = env.types();
        if (typeMirror.getKind().isPrimitive()) {
            return typeUtils.boxedClass((PrimitiveType) typeMirror.actual())
                    .asType();
        }
        return typeMirror.actual();
    }

    public static RuntimeException runtime(TypeMirror typeMirror, Throwable e) {
        return runtime(e, "error for typeMirror: %s", typeMirror);
    }

    public static RuntimeException runtime(Element element, Throwable e) {
        return runtime(e, "error for element: %s", element);
    }

    public static RuntimeException runtime(Object o, Throwable e) {
        return runtime(e, "error for object: %s", o);
    }

    public static RuntimeException runtime(Throwable e, String format, Object... args) {
        String msg = String.format(Locale.US, format, args);
        return new RuntimeException(msg, e);
    }

    public static <R, T> ImmutableList<R> buildList(Iterable<? extends T> in, Collector<R, T> collector) {
        return build(ImmutableList.<R>builder(), in, collector);
    }

    public static <R, T> ImmutableList<R> buildList(Iterable<? extends T> in, Transformer<R, T> transformer) {
        return build(ImmutableList.<R>builder(), in, transformer);
    }

    public static <R, T> ImmutableList<R> buildList(Collection<? extends T> in, Transformer<R, T> transformer) {
        if (in == null || in.isEmpty()) {
            return ImmutableList.of();
        }
        return build(ImmutableList.<R>builder(), in, transformer);
    }

    public static <R, T> ImmutableList<R> buildList(Collection<? extends T> in, Collector<R, T> collector) {
        if (in == null || in.isEmpty()) {
            return ImmutableList.of();
        }
        return build(ImmutableList.<R>builder(), in, collector);
    }

    public static <R, T> ImmutableSet<R> buildSet(Collection<? extends T> in, Transformer<R, T> transformer) {
        if (in == null || in.isEmpty()) {
            return ImmutableSet.of();
        }
        return build(ImmutableSet.<R>builder(), in, transformer);
    }

    public static <R, T> ImmutableSet<R> buildSet(Collection<? extends T> in, Collector<R, T> collector) {
        if (in == null || in.isEmpty()) {
            return ImmutableSet.of();
        }
        return build(ImmutableSet.<R>builder(), in, collector);
    }

    public static <R, T> ImmutableList<R> buildList(T[] in, Transformer<R, T> transformer) {
        return buildList(in == null ? null : Arrays.asList(in), transformer);
    }

    public static <R, T> ImmutableSet<R> buildSet(T[] in, Transformer<R, T> transformer) {
        return buildSet(in == null ? null : Arrays.asList(in), transformer);
    }

    @SuppressWarnings("unchecked")
    private static <R, T, C extends ImmutableCollection<R>> C build(ImmutableCollection.Builder<R> out,
            Iterable<? extends T> in, Transformer<R, T> transformer) {
        for (T t : in) {
            R r;
            if (t != null && (r = transformer.transform(t)) != null) {
                out.add(r);
            }
        }
        return (C) out.build();
    }

    @SuppressWarnings("unchecked")
    private static <R, T, C extends ImmutableCollection<R>> C build(ImmutableCollection.Builder<R> out,
            Iterable<? extends T> in, Collector<R, T> collector) {
        for (T t : in) {
            if (t != null) {
                collector.collect(t, out);
            }
        }
        return (C) out.build();
    }

    public static <KR, VR, KT, VT> ImmutableMap<KR, VR> buildMap(Map<? extends KT, ? extends VT> in,
            Transformer<KR, KT> keyTransformer, Transformer<VR, VT> valueTransformer) {
        ImmutableMap.Builder<KR, VR> out = ImmutableMap.builder();
        for (Map.Entry<? extends KT, ? extends VT> entry : in.entrySet()) {
            out.put(keyTransformer.transform(entry.getKey()), valueTransformer.transform(entry.getValue()));
        }
        return out.build();
    }

    public static <VR, KT, VT> ImmutableMap<KT, VR> buildMap(Map<? extends KT, ? extends VT> in,
            Transformer<VR, VT> valueTransformer) {
        ImmutableMap.Builder<KT, VR> out = ImmutableMap.builder();
        for (Map.Entry<? extends KT, ? extends VT> entry : in.entrySet()) {
            out.put(entry.getKey(), valueTransformer.transform(entry.getValue()));
        }
        return out.build();
    }

    public static <K, V> ImmutableMap<K, ImmutableSet<V>> combine(ImmutableMap<K, ImmutableSet<V>> one,
            Map<K, ImmutableSet<V>> _two) {
        // copy in order to remove entries
        Map<K, ImmutableSet<V>> two = new HashMap<>(_two);
        ImmutableMap.Builder<K, ImmutableSet<V>> out = ImmutableMap.builder();
        for (Map.Entry<K, ImmutableSet<V>> entry : one.entrySet()) {
            K key = entry.getKey();
            ImmutableSet<V> val = entry.getValue();
            // get and remove from two
            ImmutableSet<V> twoVal = two.remove(key);
            if (twoVal != null) {
                val = ImmutableSet.<V>builder()
                        .addAll(val)
                        .addAll(twoVal)
                        .build();
            }
            if (!val.isEmpty()) {
                out.put(key, val);
            }
        }
        // iterate through entries for keys only present in two
        for (Map.Entry<K, ImmutableSet<V>> entry : two.entrySet()) {
            ImmutableSet<V> val = entry.getValue();
            if (!val.isEmpty()) {
                out.put(entry.getKey(), val);
            }
        }
        return out.build();
    }

    public static String className(ClassName typeName) {
        return className(typeName.simpleNames());
    }

    public static String className(List<String> simpleNames) {
        return Joiner.on('.')
                .join(simpleNames);
    }

    public static ClassName typeName(String packageName, String className) {
        if (className.isEmpty()) throw new IllegalArgumentException("empty className");
        int index = className.indexOf('.');
        if (index == -1) {
            return ClassName.get(packageName, className);
        }

        // Add the class names, like "Map" and "Entry".
        String[] parts = className.substring(index + 1).split("\\.", -1);
        return ClassName.get(packageName, className, parts);
    }

    public static String qualifiedName(ClassName typeName) {
        String packageName = typeName.packageName();
        if (packageName.isEmpty()) {
            return className(typeName);
        }
        List<String> names = new ArrayList<>(typeName.simpleNames());
        names.add(0, packageName);
        return Joiner.on('.')
                .join(names);
    }

    public static String qualifiedName(String packageName, String className) {
        return qualifiedName(typeName(packageName, className));
    }

    public static TypeElement parse(TypeMirror typeMirror, Types typeUtils) {
        return (TypeElement) typeUtils.asElement(typeMirror);
    }

    public static TypeElement parse(Class<?> clazz, Elements elementUtils) {
        return elementUtils.getTypeElement(clazz.getCanonicalName());
    }

    public static TypeElement parse(Func0<Class<?>> classFunc, Env env) {
        final Types typeUtils = env.types();
        final Elements elementUtils = env.elements();
        try {
            return parse(classFunc.call(), elementUtils);
        } catch (MirroredTypeException e) {
            return parse(e.getTypeMirror(), typeUtils);
        }
    }

    public static ImmutableList<TypeElement> parseList(Func0<Class<?>[]> classesFunc, Env env) {
        try {
            final Elements elementUtils = env.elements();
            return buildList(classesFunc.call(), new Transformer<TypeElement, Class<?>>() {
                @Override public TypeElement transform(Class<?> clazz) {
                    return parse(clazz, elementUtils);
                }
            });
        } catch (MirroredTypesException e) {
            final Types typeUtils = env.types();
            return buildList(e.getTypeMirrors(), new Transformer<TypeElement, TypeMirror>() {
                @Override public TypeElement transform(TypeMirror typeMirror) {
                    return parse(typeMirror, typeUtils);
                }
            });
        }
    }

    public static TypeElementAlias parseAlias(Func0<Class<?>> classFunc, Env env) {
        TypeElement typeElement = parse(classFunc, env);
        return typeElement == null ? null : AliasElements.get(typeElement);
    }

    public static ImmutableList<TypeElementAlias> parseAliasList(Func0<Class<?>[]> classesFunc, Env env) {
        return buildList(parseList(classesFunc, env), new Transformer<TypeElementAlias, TypeElement>() {
            @Override public TypeElementAlias transform(TypeElement typeElement) {
                return AliasElements.get(typeElement);
            }
        });
    }

    static void copyTypeParams(ExecutableElement method, MethodSpec.Builder spec) {
        for (TypeParameterElement typeParameterElement : method.getTypeParameters()) {
            String typeParamName = typeParameterElement.getSimpleName().toString();
            List<TypeName> bounds = new ArrayList<>();
            for (TypeMirror bound : typeParameterElement.getBounds()) {
                bounds.add(TypeName.get(bound));
            }
            spec.addTypeVariable(
                    TypeVariableName.get(typeParamName, bounds.toArray(new TypeName[bounds.size()])));
        }
    }

    public static String copyParameters(ExecutableElement method, MethodSpec.Builder spec) {
        List<String> paramNames = new ArrayList<>();
        for (VariableElement param : method.getParameters()) {
            String paramName = param.getSimpleName().toString();
            paramNames.add(paramName);
            TypeName paramType = TypeName.get(param.asType());
            Set<Modifier> modifiers = param.getModifiers();
            ParameterSpec.Builder paramSpec = ParameterSpec.builder(paramType, paramName)
                    .addModifiers(modifiers.toArray(new Modifier[modifiers.size()]));
            for (AnnotationMirror am : method.getAnnotationMirrors()) {
                TypeElement te = (TypeElement) am.getAnnotationType().asElement();
                paramSpec.addAnnotation(ClassName.get(te));
            }
            spec.addParameter(paramSpec.build());
        }
        boolean first = true;
        StringBuilder paramString = new StringBuilder();
        for (String paramName : paramNames) {
            if (!first) {
                paramString.append(", ");
            }
            first = false;
            paramString.append(paramName);
        }
        return paramString.toString();
    }

    static String copyParameters(ExecutableElement method, MethodSpec.Builder spec,
            ContainerType containerType, Types typeUtils) {
        List<String> paramNames = new ArrayList<>();
        for (VariableElement param : method.getParameters()) {
            String paramName = param.getSimpleName().toString();
            paramNames.add(paramName);
            TypeName paramType = TypeName.get(param.asType());
            if (paramType instanceof ParameterizedTypeName) {
                ParameterizedTypeName ptn = (ParameterizedTypeName) paramType;
                paramType = coallesceParamType(ptn.rawType, ptn.typeArguments, containerType, typeUtils);
            }

            Set<Modifier> modifiers = param.getModifiers();
            ParameterSpec.Builder paramSpec = ParameterSpec.builder(paramType, paramName)
                    .addModifiers(modifiers.toArray(new Modifier[modifiers.size()]));
            for (AnnotationMirror am : method.getAnnotationMirrors()) {
                TypeElement te = (TypeElement) am.getAnnotationType().asElement();
                paramSpec.addAnnotation(ClassName.get(te));
            }
            spec.addParameter(paramSpec.build());
        }
        boolean first = true;
        StringBuilder paramString = new StringBuilder();
        for (String paramName : paramNames) {
            if (!first) {
                paramString.append(", ");
            }
            first = false;
            paramString.append(paramName);
        }
        return paramString.toString();
    }

    static ParameterizedTypeName builder(TypeName ofType) {
        return paramType(Builder, ofType);
    }

    static ParameterizedTypeName list(TypeName ofType) {
        return paramType(List, ofType);
    }

    static ParameterizedTypeName wildcardList(TypeName ofType) {
        return list(WildcardTypeName.subtypeOf(ofType));
    }

    private static ParameterizedTypeName twoType(ClassName rawType, List<? extends TypeName> paramTypes) {
        if (paramTypes.size() != 2) {
            throw new IllegalArgumentException("must be 2 Type Parameters. Instead contains " + paramTypes.size());
        }
        return (ParameterizedTypeName) paramType(rawType, paramTypes);
    }

    static List<TypeVariableName> parseTypeParams(TypeElement typeElement) {
        List<TypeVariableName> typeParams = new ArrayList<>();
        for (TypeParameterElement typeParam : typeElement.getTypeParameters()) {
            typeParams.add((TypeVariableName) TypeName.get(typeParam.asType()));
        }
        return Collections.unmodifiableList(typeParams);
    }

    static TypeName paramType(ClassName rawType, List<? extends TypeName> paramTypes) {
        if (paramTypes.isEmpty()) {
            return rawType;
        }
        return paramType(rawType, paramTypes.toArray(new TypeName[paramTypes.size()]));
    }

    static TypeName coallesceParamType(ClassName rawType, List<? extends TypeName> paramTypes,
            ContainerType containerType, Types typeUtils) {
        List<? extends TypeMirror> containerTypes = new ArrayList<>(containerType.parameterizedType.typeArguments);
        List<TypeName> types = new ArrayList<>();

        NEXT:
        for (TypeName paramType : paramTypes) {
            ProxyLog.d(TAG, "paramTypeName: %s", paramType);
            if (!(paramType instanceof TypeVariableName)) {
                if (paramType instanceof ParameterizedTypeName) {
                    ParameterizedTypeName ptn = (ParameterizedTypeName) paramType;
                    paramType = coallesceParamType(ptn.rawType, ptn.typeArguments, containerType, typeUtils);
                } else {
                    ProxyLog.d(TAG, "Don't know how to handle typeName: %s", paramType);
                }
                types.add(paramType);
                continue;
            }
            ProxyLog.d(TAG, "paramTypeBounds: %s", ((TypeVariableName) paramType).bounds);
            TypeVariableName typeName = (TypeVariableName) paramType;
            List<TypeName> bounds = typeName.bounds;
            if (bounds.size() != 0) {
                Iterator<? extends TypeMirror> containerTypesIterator = containerTypes.iterator();
                while (containerTypesIterator.hasNext()) {
                    TypeMirror containerMirror = containerTypesIterator.next();
                    TypeName containerTypeName = TypeName.get(containerMirror);
                    TypeName found = findTypeName(containerMirror, bounds, typeUtils);
                    if (found != null) {
                        ProxyLog.d(TAG, "found super type: %s", found);
                        types.add(containerTypeName);
                        containerTypesIterator.remove();
                        continue NEXT;
                    }
                }
            }
            types.add(paramType);
        }
        ProxyLog.d(TAG, "finalTypes: %s", types);
        return paramType(rawType, types);
    }

    private static TypeName findTypeName(TypeMirror type, List<? extends TypeName> bounds, Types typeUtils) {
        if (type == null || type.getKind() == TypeKind.NONE) return null;
        TypeName typeName = TypeName.get(type);
        ProxyLog.d(TAG, "checking type: %s", typeName);
        if (contains(bounds, typeName)) {
            ProxyLog.d(TAG, "found type: %s", typeName);
            return typeName;
        }
        Element element = typeUtils.asElement(type);
        if (element.getKind() == ElementKind.CLASS || element.getKind() == ElementKind.INTERFACE) {
            TypeElement typeElement = (TypeElement) element;
            TypeName found = findTypeName(typeElement.getSuperclass(), bounds, typeUtils);
            if (found != null) {
                return found;
            }
            for (TypeMirror interfaceType : typeElement.getInterfaces()) {
                found = findTypeName(interfaceType, bounds, typeUtils);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static boolean contains(List<? extends TypeName> bounds, TypeName type) {
        if (bounds.contains(type)) {
            return true;
        }
        if (type instanceof ParameterizedTypeName) {
            ParameterizedTypeName ptn = (ParameterizedTypeName) type;
            for (TypeName bound : bounds) {
                if (bound instanceof ParameterizedTypeName) {
                    if (ptn.rawType.equals(((ParameterizedTypeName) bound).rawType)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    static ParameterizedTypeName paramType(ClassName rawType, TypeName... paramTypes) {
        return ParameterizedTypeName.get(rawType, paramTypes);
    }

    static ContainerType parseBaseApiType(TypeElement api, Types typeUtils) {
        ContainerType baseApiType = null;
        TypeMirror superClass = api.asType();
        while (baseApiType == null && superClass.getKind() != TypeKind.NONE) {
            TypeName superClassType = TypeName.get(superClass);
            if (superClassType instanceof ParameterizedTypeName) {
                ParameterizedTypeName ptn = (ParameterizedTypeName) superClassType;
                if (ptn.typeArguments.size() == 2) {
                    baseApiType = new ContainerType(superClassType, superClass);
                }
            }
            if (baseApiType == null) {
                TypeElement superElement = (TypeElement) typeUtils.asElement(superClass);
                superClass = superElement.getSuperclass();
            }
        }
        if (baseApiType == null) {
            throw new IllegalStateException("Api must extend from " + PACKAGE + ".BaseApi");
        }
        return baseApiType;
    }

    static final class ContainerType {
        final TypeName typeName;
        final List<TypeName> typeArguments;
        final TypeMirror type;
        final ParameterizedType parameterizedType;

        private ContainerType(TypeName typeName, TypeMirror type) {
            this.typeName = typeName;
            this.type = type;
            this.parameterizedType = type.accept(new SimpleTypeVisitor7<ParameterizedType, Void>() {
                @Override public ParameterizedType visitDeclared(DeclaredType t, Void p) {
                    return new ParameterizedType(t);
                }
            }, null);
            List<TypeName> typeArguments = new ArrayList<>();
            for (TypeMirror mirror : parameterizedType.typeArguments) {
                typeArguments.add(TypeName.get(mirror));
            }
            this.typeArguments = Collections.unmodifiableList(typeArguments);
        }
    }

    static final class ParameterizedType {
        final ClassName rawType;
        final List<TypeMirror> typeArguments;

        public ParameterizedType(DeclaredType t) {
            this.rawType = ClassName.get((TypeElement) t.asElement());
            this.typeArguments = Collections.unmodifiableList(t.getTypeArguments());
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Transformer<T, T> noopTransformer() {
        return (Transformer<T, T>) NOOP_TRANSFORMER;
    }

    private static final Transformer NOOP_TRANSFORMER = new Transformer() {
        @Override public Object transform(Object o) {
            return o;
        }
    };

    public interface Transformer<R, T> {
        R transform(T t);
    }

    public interface Collector<R, T> {
        void collect(T t, ImmutableCollection.Builder<R> out);
    }

    public interface ArrayCreator<T> {
        T[] newArray(int size);
    }

    private Util() { throw new AssertionError("no instances"); }
}
