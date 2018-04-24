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

import com.laynemobile.proxy.internal.ProxyLog;
import com.laynemobile.proxy.internal.Util;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a generic type {@code T}. Java doesn't yet provide a way to represent generic types, so this class does.
 * Forces clients to create a subclass of this class which enables retrieval the type information even at runtime.
 * <p>
 * <p>For example, to create a type literal for {@code List<String>}, you can create an empty anonymous inner class:
 * <p>
 * <p>
 * {@code TypeToken<List<String>> list = new TypeToken<List<String>>() {};}
 * <p>
 * <p>This syntax cannot be used to create type literals that have wildcard parameters, such as {@code Class<?>} or
 * {@code List<? extends CharSequence>}.
 *
 * @author Bob Lee
 * @author Sven Mawson
 * @author Jesse Wilson
 */
public class TypeToken<T> {
    private static final String TAG = TypeToken.class.getSimpleName();

    final Class<? super T> rawType;
    final Type type;
    final int hashCode;

    /**
     * Constructs a new type literal. Derives represented class from type parameter.
     * <p>
     * <p>Clients create an empty anonymous subclass. Doing so embeds the type parameter in the anonymous class's type
     * hierarchy so we can reconstitute it at runtime despite erasure.
     */
    @SuppressWarnings("unchecked") protected TypeToken() {
        this.type = getSuperclassTypeParameter(getClass());
        this.rawType = (Class<? super T>) $Proxy$Types.getRawType(type);
        this.hashCode = type.hashCode();
    }

    /**
     * Unsafe. Constructs a type literal manually.
     */
    @SuppressWarnings("unchecked") TypeToken(Type type) {
        this.type = $Proxy$Types.canonicalize(Util.checkNotNull(type));
        this.rawType = (Class<? super T>) $Proxy$Types.getRawType(this.type);
        this.hashCode = this.type.hashCode();
    }

    /**
     * Returns the type from super class's type parameter in {@link $Proxy$Types#canonicalize canonical form}.
     */
    static Type getSuperclassTypeParameter(Class<?> subclass) {
        Type superclass = subclass.getGenericSuperclass();
        if (superclass instanceof Class) {
            throw new RuntimeException("Missing type parameter.");
        }
        ParameterizedType parameterized = (ParameterizedType) superclass;
        return $Proxy$Types.canonicalize(parameterized.getActualTypeArguments()[0]);
    }

    public static TypeToken<?>[] getTypeParameters(Class<?> clazz) {
        return getTypeParameters(clazz.getGenericSuperclass());
    }

    public static TypeToken<?>[] getTypeParameters(TypeToken<?> typeToken) {
        return getTypeParameters(typeToken.getType());
    }

    public static TypeToken<?>[] getSuperInterfaceTypeParameters(Class<?> clazz, Class<?> superInterfaceType) {
        for (Type test : clazz.getGenericInterfaces()) {
            ProxyLog.d(TAG, "interface type: %s", test);
            if (test instanceof Class) {
                log(test);
                continue;
            }
            ParameterizedType parameterized = (ParameterizedType) test;
            ProxyLog.d(TAG, "interface parameterizedType: %s", parameterized);
            Type rawType = parameterized.getRawType();
            if (rawType.equals(superInterfaceType)) {
                return getTypeParameters(parameterized);
            }
        }
        return new TypeToken<?>[0];
    }

    private static void log(Type type) {
        if (type instanceof Class<?>) {
            log(null, (Class<?>) type);
        } else {
            log(type, type.getClass());
        }
    }

    private static void log(Object instance, Class<?> clazz) {
//        ProxyLog.d(TAG, "class: %s, instance: %s", clazz, instance);
//        for (Field field : clazz.getDeclaredFields()) {
//            if (!field.isAccessible()) {
//                field.setAccessible(true);
//            }
//            try {
//                Object value = field.get(instance);
//                ProxyLog.d(TAG, "class: %s, field: %s, value: %s", clazz, field, value);
//            } catch (Throwable e) {
//                ProxyLog.d(TAG, "class: %s, field: %s", clazz, field);
//            }
//        }
//        Class<?> superClass = clazz.getSuperclass();
//        if (superClass != null && !Object.class.equals(superClass)) {
//            log(instance, superClass);
//        }
    }

    public static TypeToken<?>[] getSuperTypeParameters(Class<?> clazz, Class<?> superType) {
        Type test = clazz.getGenericSuperclass();
        while (!(test instanceof Class)) {
            ParameterizedType parameterized = (ParameterizedType) test;
            ProxyLog.d(TAG, "super parameterizedType: %s", parameterized);
            Type superClass = parameterized.getRawType();
            if (!(superClass instanceof Class)) {
                throw new IllegalArgumentException("unknown rawType: " + superClass);
            }
            if (superClass.equals(superType)) {
                return getTypeParameters(parameterized);
            }
            test = ((Class<?>) superClass).getGenericSuperclass();
        }
        log(test);
        return new TypeToken<?>[0];
    }

    @SuppressWarnings("unchecked")
    public static <T> TypeToken<T> getTypeParameter(Class<?> clazz) {
        TypeToken<?>[] typeParameters = getTypeParameters(clazz);
        if (typeParameters.length != 1) {
            throw new IllegalArgumentException("more than one type parameter");
        }
        return (TypeToken<T>) typeParameters[0];
    }

    @SuppressWarnings("unchecked")
    public static <T> TypeToken<T> getSuperTypeParameter(Class<?> clazz, Class<?> superType) {
        TypeToken<?>[] typeParameters = getSuperTypeParameters(clazz, superType);
        if (typeParameters.length != 1) {
            throw new IllegalArgumentException("more than one type parameter");
        }
        return (TypeToken<T>) typeParameters[0];
    }

    private static TypeToken<?>[] getTypeParameters(Type type) {
        if (type instanceof Class) {
            log(type);
            return new TypeToken<?>[0];
        }
        return getTypeParameters((ParameterizedType) type);
    }

    private static TypeToken<?>[] getTypeParameters(ParameterizedType parameterized) {
        log(parameterized);
        Type[] typeArguments = parameterized.getActualTypeArguments();
        int length = typeArguments.length;
        TypeToken<?>[] tokens = new TypeToken<?>[length];
        for (int i = 0; i < length; i++) {
            Type type = $Proxy$Types.canonicalize(typeArguments[i]);
            tokens[i] = get(type);
        }
        return tokens;
    }

    /**
     * Returns the raw (non-generic) type for this type.
     */
    public final Class<? super T> getRawType() {
        return rawType;
    }

    /**
     * Gets underlying {@code Type} instance.
     */
    public final Type getType() {
        return type;
    }

    /**
     * Check if this type is assignable from the given class object.
     *
     * @deprecated this implementation may be inconsistent with javac for types with wildcards.
     */
    @Deprecated
    public boolean isAssignableFrom(Class<?> cls) {
        return isAssignableFrom((Type) cls);
    }

    /**
     * Check if this type is assignable from the given Type.
     *
     * @deprecated this implementation may be inconsistent with javac for types with wildcards.
     */
    @Deprecated
    public boolean isAssignableFrom(Type from) {
        if (from == null) {
            return false;
        }

        if (type.equals(from)) {
            return true;
        }

        if (type instanceof Class<?>) {
            return rawType.isAssignableFrom($Proxy$Types.getRawType(from));
        } else if (type instanceof ParameterizedType) {
            return isAssignableFrom(from, (ParameterizedType) type,
                    new HashMap<String, Type>());
        } else if (type instanceof GenericArrayType) {
            return rawType.isAssignableFrom($Proxy$Types.getRawType(from))
                    && isAssignableFrom(from, (GenericArrayType) type);
        } else {
            throw buildUnexpectedTypeError(
                    type, Class.class, ParameterizedType.class, GenericArrayType.class);
        }
    }

    /**
     * Check if this type is assignable from the given type token.
     *
     * @deprecated this implementation may be inconsistent with javac for types with wildcards.
     */
    @Deprecated
    public boolean isAssignableFrom(TypeToken<?> token) {
        return isAssignableFrom(token.getType());
    }

    /**
     * Private helper function that performs some assignability checks for the provided GenericArrayType.
     */
    private static boolean isAssignableFrom(Type from, GenericArrayType to) {
        Type toGenericComponentType = to.getGenericComponentType();
        if (toGenericComponentType instanceof ParameterizedType) {
            Type t = from;
            if (from instanceof GenericArrayType) {
                t = ((GenericArrayType) from).getGenericComponentType();
            } else if (from instanceof Class<?>) {
                Class<?> classType = (Class<?>) from;
                while (classType.isArray()) {
                    classType = classType.getComponentType();
                }
                t = classType;
            }
            return isAssignableFrom(t, (ParameterizedType) toGenericComponentType,
                    new HashMap<String, Type>());
        }
        // No generic defined on "to"; therefore, return true and let other
        // checks determine assignability
        return true;
    }

    /**
     * Private recursive helper function to actually do the type-safe checking of assignability.
     */
    private static boolean isAssignableFrom(Type from, ParameterizedType to,
            Map<String, Type> typeVarMap) {

        if (from == null) {
            return false;
        }

        if (to.equals(from)) {
            return true;
        }

        // First figure out the class and any type information.
        Class<?> clazz = $Proxy$Types.getRawType(from);
        ParameterizedType ptype = null;
        if (from instanceof ParameterizedType) {
            ptype = (ParameterizedType) from;
        }

        // Load up parameterized variable info if it was parameterized.
        if (ptype != null) {
            Type[] tArgs = ptype.getActualTypeArguments();
            TypeVariable<?>[] tParams = clazz.getTypeParameters();
            for (int i = 0; i < tArgs.length; i++) {
                Type arg = tArgs[i];
                TypeVariable<?> var = tParams[i];
                while (arg instanceof TypeVariable<?>) {
                    TypeVariable<?> v = (TypeVariable<?>) arg;
                    arg = typeVarMap.get(v.getName());
                }
                typeVarMap.put(var.getName(), arg);
            }

            // check if they are equivalent under our current mapping.
            if (typeEquals(ptype, to, typeVarMap)) {
                return true;
            }
        }

        for (Type itype : clazz.getGenericInterfaces()) {
            if (isAssignableFrom(itype, to, new HashMap<String, Type>(typeVarMap))) {
                return true;
            }
        }

        // Interfaces didn't work, try the superclass.
        Type sType = clazz.getGenericSuperclass();
        return isAssignableFrom(sType, to, new HashMap<String, Type>(typeVarMap));
    }

    /**
     * Checks if two parameterized types are exactly equal, under the variable replacement described in the typeVarMap.
     */
    private static boolean typeEquals(ParameterizedType from,
            ParameterizedType to, Map<String, Type> typeVarMap) {
        if (from.getRawType().equals(to.getRawType())) {
            Type[] fromArgs = from.getActualTypeArguments();
            Type[] toArgs = to.getActualTypeArguments();
            for (int i = 0; i < fromArgs.length; i++) {
                if (!matches(fromArgs[i], toArgs[i], typeVarMap)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private static AssertionError buildUnexpectedTypeError(
            Type token, Class<?>... expected) {

        // Build exception message
        StringBuilder exceptionMessage =
                new StringBuilder("Unexpected type. Expected one of: ");
        for (Class<?> clazz : expected) {
            exceptionMessage.append(clazz.getName()).append(", ");
        }
        exceptionMessage.append("but got: ").append(token.getClass().getName())
                .append(", for type token: ").append(token.toString()).append('.');

        return new AssertionError(exceptionMessage.toString());
    }

    /**
     * Checks if two types are the same or are equivalent under a variable mapping given in the type map that was
     * provided.
     */
    private static boolean matches(Type from, Type to, Map<String, Type> typeMap) {
        return to.equals(from)
                || (from instanceof TypeVariable
                && to.equals(typeMap.get(((TypeVariable<?>) from).getName())));
    }

    @Override public final int hashCode() {
        return this.hashCode;
    }

    @Override public final boolean equals(Object o) {
        return o instanceof TypeToken<?>
                && $Proxy$Types.equals(type, ((TypeToken<?>) o).type);
    }

    @Override public final String toString() {
        return $Proxy$Types.typeToString(type);
    }

    /**
     * Gets type literal for the given {@code Type} instance.
     */
    public static TypeToken<?> get(Type type) {
        return new TypeToken<Object>(type);
    }

    /**
     * Gets type literal for the given {@code Class} instance.
     */
    public static <T> TypeToken<T> get(Class<T> type) {
        return new TypeToken<T>(type);
    }

    public static <T> TypeToken<? extends T> get(T t) {
        if (t == null) {
            throw new NullPointerException("t is null");
        }
        @SuppressWarnings("unchecked")
        Class<? extends T> clazz = (Class<? extends T>) t.getClass();
        return get(clazz);
    }
}
