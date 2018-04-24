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

package com.laynemobile.proxy.elements;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.laynemobile.proxy.Util;
import com.laynemobile.proxy.types.AliasTypes;

import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor7;

import static com.laynemobile.proxy.Util.buildList;

final class DefaultAnnotationValueAlias implements AnnotationValueAlias {
    private final Value value;
    private final String toString;

    private DefaultAnnotationValueAlias(AnnotationValue annotationValue) {
        this.value = annotationValue.accept(new Visitor7(), null);
        this.toString = annotationValue.toString();
    }

    static AnnotationValueAlias of(AnnotationValue annotationValue) {
        if (annotationValue instanceof AnnotationValueAlias) {
            return (AnnotationValueAlias) annotationValue;
        }
        return annotationValue == null ? null : new DefaultAnnotationValueAlias(annotationValue);
    }

    static ImmutableList<? extends AnnotationValueAlias> of(List<? extends AnnotationValue> annotationValues) {
        return buildList(annotationValues, new Util.Transformer<AnnotationValueAlias, AnnotationValue>() {
            @Override public AnnotationValueAlias transform(AnnotationValue annotationValue) {
                return of(annotationValue);
            }
        });
    }

    @Override public Object getValue() {
        return value.value();
    }

    @SuppressWarnings("unchecked")
    @Override public <R, P> R accept(AnnotationValueVisitor<R, P> v, P p) {
        Object o = value.value();
        switch (value.kind()) {
            case Boolean:
                return v.visitBoolean((boolean) o, p);
            case Byte:
                return v.visitByte((byte) o, p);
            case Char:
                return v.visitChar((char) o, p);
            case Double:
                return v.visitDouble((double) o, p);
            case Float:
                return v.visitFloat((float) o, p);
            case Int:
                return v.visitInt((int) o, p);
            case Long:
                return v.visitLong((long) o, p);
            case Short:
                return v.visitShort((short) o, p);
            case Array:
                return v.visitArray((List<? extends AnnotationValue>) o, p);
            case String:
                return v.visitString((String) o, p);
            case Annotation:
                return v.visitAnnotation((AnnotationMirror) o, p);
            case Enum:
                return v.visitEnumConstant((VariableElement) o, p);
            case Type:
                return v.visitType((TypeMirror) o, p);
            case Unknown:
            default:
                return v.visitUnknown(this, p);
        }
    }

    @Override public String toString() {
        return toString;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultAnnotationValueAlias)) return false;
        DefaultAnnotationValueAlias that = (DefaultAnnotationValueAlias) o;
        return Objects.equal(value, that.value) &&
                Objects.equal(toString, that.toString);
    }

    @Override public int hashCode() {
        return Objects.hashCode(value, toString);
    }

    private static final class Visitor7 extends SimpleAnnotationValueVisitor7<Value, Void> {
        private Visitor7() {
            super();
        }

        @Override protected Value defaultAction(Object o, Void aVoid) {
            return new Value(Kind.Unknown, o);
        }

        @Override public Value visitBoolean(boolean b, Void aVoid) {
            return new Value(Kind.Boolean, b);
        }

        @Override public Value visitByte(byte b, Void aVoid) {
            return new Value(Kind.Byte, b);
        }

        @Override public Value visitChar(char c, Void aVoid) {
            return new Value(Kind.Char, c);
        }

        @Override public Value visitDouble(double d, Void aVoid) {
            return new Value(Kind.Double, d);
        }

        @Override public Value visitFloat(float f, Void aVoid) {
            return new Value(Kind.Float, f);
        }

        @Override public Value visitInt(int i, Void aVoid) {
            return new Value(Kind.Int, i);
        }

        @Override public Value visitLong(long i, Void aVoid) {
            return new Value(Kind.Long, i);
        }

        @Override public Value visitShort(short s, Void aVoid) {
            return new Value(Kind.Short, s);
        }

        @Override public Value visitArray(List<? extends AnnotationValue> vals, Void aVoid) {
            return new Value(Kind.Array, of(vals));
        }

        @Override public Value visitString(String s, Void aVoid) {
            return new Value(Kind.String, s);
        }

        @Override public Value visitAnnotation(AnnotationMirror a, Void aVoid) {
            return new Value(Kind.Annotation, DefaultAnnotationMirrorAlias.of(a));
        }

        @Override public Value visitEnumConstant(VariableElement c, Void aVoid) {
            return new Value(Kind.Enum, AliasElements.get(c));
        }

        @Override public Value visitType(TypeMirror t, Void aVoid) {
            return new Value(Kind.Type, AliasTypes.get(t));
        }

        @Override public Value visitUnknown(AnnotationValue av, Void aVoid) {
            return new Value(Kind.Unknown, av.getValue());
        }
    }

    private enum Kind {
        Boolean,
        Byte,
        Char,
        Double,
        Float,
        Int,
        Long,
        Short,
        Array,
        String,
        Annotation,
        Enum,
        Type,
        Unknown
    }

    private static final class Value {
        private final Kind kind;
        private final Object value;

        private Value(Kind kind, Object value) {
            this.kind = kind;
            this.value = value;
        }

        private Kind kind() {
            return kind;
        }

        private Object value() {
            return value;
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Value)) return false;
            Value that = (Value) o;
            return kind == that.kind &&
                    Objects.equal(value, that.value);
        }

        @Override public int hashCode() {
            return Objects.hashCode(kind, value);
        }
    }
}
