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

package com.laynemobile.proxy.output;

import com.laynemobile.proxy.TypeToken;
import com.laynemobile.proxy.Util;
import com.laynemobile.proxy.elements.TypeParameterElementAlias;
import com.laynemobile.proxy.AnnotatedProxyElement;
import com.laynemobile.proxy.ProxyFunctionElement;
import com.laynemobile.proxy.types.AliasTypes;
import com.laynemobile.proxy.types.TypeMirrorAlias;
import com.laynemobile.proxy.types.TypeVariableAlias;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import com.squareup.javapoet.WildcardTypeName;

import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;

import static java.util.Locale.US;

public class ProxyFunctionAbstractTypeOutputStub extends AbstractTypeElementOutputStub<ProxyFunctionAbstractTypeOutput> {
    private static final String ABSTRACT_PREFIX = "Abstract";
    private static final String PROXY_SUFFIX = "Proxy";

    private final AnnotatedProxyElement parent;
    private final ProxyFunctionElement function;
    private final ExecutableElement element;
    private final String basePackageName;
    private final String baseClassName;
    private final TypeMirror superClass;

    private ProxyFunctionAbstractTypeOutputStub(AnnotatedProxyElement parent, ProxyFunctionElement function,
            String baseClassName) {
        this(parent, function, parent.packageName() + ".functions", baseClassName);
    }

    private ProxyFunctionAbstractTypeOutputStub(AnnotatedProxyElement parent, ProxyFunctionElement function,
            String basePackageName,
            String baseClassName) {
        super(basePackageName + ".parent", ABSTRACT_PREFIX + baseClassName);
        this.parent = parent;
        this.function = function;
        this.element = function.element();
        this.basePackageName = basePackageName;
        this.baseClassName = baseClassName;
        this.superClass = function.abstractProxyFunctionType();
    }

    private ProxyFunctionAbstractTypeOutputStub(ProxyFunctionAbstractTypeOutputStub source, TypeMirror superClass) {
        super(source.packageName(), source.className());
        this.parent = source.parent;
        this.function = source.function;
        this.element = source.element;
        this.basePackageName = source.basePackageName;
        this.baseClassName = source.baseClassName;
        this.superClass = superClass;
    }

    public static ProxyFunctionAbstractTypeOutputStub create(ProxyFunctionElement function) {
        AnnotatedProxyElement parent = AnnotatedProxyElement.cache().get(function.parent());
        ExecutableElement element = function.element();
        String parammys = "";
        for (TypeMirror paramType : function.alias().paramTypes()) {
            if (parammys.isEmpty()) {
                parammys += "__";
            } else {
                parammys += "_";
            }
            TypeKind kind = paramType.getKind();
            if (kind.isPrimitive()) {
                parammys += kind.name().toLowerCase(US);
            } else if (kind == TypeKind.DECLARED) {
                parammys += ((DeclaredType) paramType).asElement().getSimpleName();
            } else if (kind == TypeKind.TYPEVAR) {
                parammys += ((TypeVariable) paramType).asElement().getSimpleName();
            } else {
                throw new IllegalStateException("unknown param type: " + paramType);
            }
        }

        String baseClassName = String.format(US, "%s%s_%s%s",
                parent.element().element().getSimpleName(), PROXY_SUFFIX, element.getSimpleName(), parammys);
        return new ProxyFunctionAbstractTypeOutputStub(parent, function, baseClassName);
    }

    String baseClassName() {
        return baseClassName;
    }

    String basePackageName() {
        return basePackageName;
    }

    ExecutableElement element() {
        return element;
    }

    ProxyFunctionElement function() {
        return function;
    }

    AnnotatedProxyElement parent() {
        return parent;
    }

    TypeMirror superClass() {
        return superClass;
    }

    public ProxyFunctionAbstractTypeOutputStub withSuperClass(TypeMirror superClass) {
        return new ProxyFunctionAbstractTypeOutputStub(this, superClass);
    }

    @Override protected TypeSpec build(TypeSpec.Builder classBuilder) {
        classBuilder = classBuilder.superclass(TypeName.get(superClass))
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
        // TODO: add annotation!
//                .addAnnotation(AnnotationSpec.builder(ProxyFunctionImplementation.class)
//                        .addMember("value", "$T.class", subclass)
//                        .build())
        ;

        List<TypeVariableAlias> typeVariables = Util.buildList(parent.element().element().getTypeParameters(),
                new Util.Transformer<TypeVariableAlias, TypeParameterElementAlias>() {
                    @Override
                    public TypeVariableAlias transform(TypeParameterElementAlias typeParameterElementAlias) {
                        TypeMirrorAlias type = typeParameterElementAlias.asType();
                        if (type.getKind() == TypeKind.TYPEVAR) {
                            return AliasTypes.get((TypeVariable) type.actual());
                        }
                        return null;
                    }
                });

        for (TypeVariableAlias typeVariable : typeVariables) {
            classBuilder.addTypeVariable(TypeVariableName.get(typeVariable.actual()));
        }

        String name = function.name();

        // param types field
        ClassName typeTokenType = ClassName.get(TypeToken.class);
        TypeName typeTokenWildcardType = ParameterizedTypeName.get(typeTokenType,
                WildcardTypeName.subtypeOf(ClassName.get(Object.class)));
        TypeName fieldType = ArrayTypeName.of(typeTokenWildcardType);
        FieldSpec.Builder paramTypesField = FieldSpec.builder(fieldType, "paramTypes")
                .addModifiers(Modifier.PROTECTED, Modifier.FINAL);
        CodeBlock.Builder initializer = CodeBlock.builder()
                .add("$[")
                .add("new $T[] {", typeTokenWildcardType);

        boolean first = true;
        for (TypeMirrorAlias paramType : function.alias().paramTypes()) {
            if (first) {
                initializer.add("\n");
            } else {
                initializer.add(",\n");
            }
            first = false;

            TypeMirror type = paramType.actual();
            TypeKind kind = type.getKind();
            if (kind == TypeKind.TYPEVAR) {
                type = ((TypeVariable) type).getUpperBound();
                kind = type.getKind();
            }

            if (kind.isPrimitive()) {
                String kindName = kind.name().toLowerCase(US);
                initializer.add("$T.get($L.class)", typeTokenType, kindName);
            } else if (kind == TypeKind.DECLARED && ((DeclaredType) type).getTypeArguments().isEmpty()) {
                initializer.add("$T.get($T.class)", typeTokenType, type);
            } else {
                TypeName typeName = ParameterizedTypeName.get(typeTokenType, TypeName.get(type));
                initializer.add("new $T() {}", typeName);
            }
        }
        initializer.add("$]");
        if (!first) {
            initializer.add("\n");
        }
        initializer.add("}");

        classBuilder.addField(paramTypesField
                .initializer(initializer.build())
                .build());

        // Constructor
        classBuilder.addMethod(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PROTECTED)
                .addParameter(TypeName.get(function.functionType()), name)
                .addStatement("super($L)", name)
                .build());

        // handler method
        ClassName NamedMethodHandler = ClassName.get(com.laynemobile.proxy.NamedMethodHandler.class);
        ClassName NamedMethodHandler_Builder = NamedMethodHandler.nestedClass("Builder");
        ClassName FunctionHandlers = ClassName.get(com.laynemobile.proxy.functions.FunctionHandlers.class);
        classBuilder.addMethod(MethodSpec.methodBuilder("handler")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(NamedMethodHandler)
                .addCode(CodeBlock.builder()
                        .add("$[")
                        .add("return new $T()\n", NamedMethodHandler_Builder)
                        .add(".setName($S)\n", element.getSimpleName())
                        .add(".setMethodHandler($T.from(function()))\n", FunctionHandlers)
                        .add(".build()")
                        .add(";\n$]")
                        .build())
                .build());
        return classBuilder.build();
    }

    @Override protected ProxyFunctionAbstractTypeOutput convert(TypeElementOutput output) {
        return new ProxyFunctionAbstractTypeOutput(this, output.typeSpec(), output.didWrite());
    }
}
