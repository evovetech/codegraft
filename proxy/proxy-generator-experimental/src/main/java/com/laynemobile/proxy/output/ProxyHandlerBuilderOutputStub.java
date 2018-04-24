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

import com.google.common.collect.ImmutableSet;
import com.laynemobile.proxy.Util;
import com.laynemobile.proxy.Util.Transformer;
import com.laynemobile.proxy.elements.TypeParameterElementAlias;
import com.laynemobile.proxy.AnnotatedProxyElement;
import com.laynemobile.proxy.ProxyElement;
import com.laynemobile.proxy.ProxyEnv;
import com.laynemobile.proxy.ProxyFunctionElement;
import com.laynemobile.proxy.types.AliasTypes;
import com.laynemobile.proxy.types.TypeMirrorAlias;
import com.laynemobile.proxy.types.TypeVariableAlias;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.ElementFilter;

import sourcerer.processor.Env;

import static com.laynemobile.proxy.Util.buildList;
import static com.laynemobile.proxy.Util.typeMirrorArray;
import static com.laynemobile.proxy.Util.typeNameArray;

public final class ProxyHandlerBuilderOutputStub extends DefaultTypeElementOutputStub {
    private static final String CLASS_SUFFIX = "ProxyHandlerBuilder";
    private final AnnotatedProxyElement proxyElement;
    private final ProxyEnv env;
    private final ImmutableSet<ProxyFunctionOutput> functions;

    private ProxyHandlerBuilderOutputStub(Env env, AnnotatedProxyElement proxyElement,
            Set<ProxyFunctionOutput> functions) {
        super(proxyElement.element().packageName(), className(proxyElement.element()));
        this.env = ProxyEnv.wrap(env);
        this.proxyElement = proxyElement;
        this.functions = ImmutableSet.copyOf(functions);
    }

    static ProxyHandlerBuilderOutputStub create(Env env, AnnotatedProxyElement proxyElement,
            Set<ProxyFunctionOutput> functions) {
        return new ProxyHandlerBuilderOutputStub(env, proxyElement, functions);
    }

    public static boolean exists(ProxyElement proxyElement, Env env) {
        return DefaultTypeElementOutputStub.create(packageName(proxyElement), className(proxyElement))
                .elementExists(env);
    }

    private static String packageName(ProxyElement proxyElement) {
        return proxyElement.packageName();
    }

    private static String className(ProxyElement proxyElement) {
        return proxyElement.className().simpleName() + CLASS_SUFFIX;
    }

    @Override protected TypeSpec build(TypeSpec.Builder classBuilder) {
        DeclaredType proxyType = (DeclaredType) proxyElement.element().element().asType().actual();
        List<? extends TypeMirror> wildcardTypeArguments
                = buildList(proxyType.getTypeArguments(), new Transformer<TypeMirror, TypeMirror>() {
            @Override public TypeMirror transform(TypeMirror typeMirror) {
                // Create wildcard type for each type mirror
                return env.types().getWildcardType(typeMirror, null);
            }
        });
        TypeMirror[] typeParams = typeMirrorArray(proxyType.getTypeArguments());
        TypeMirror[] wildcardTypeParams = typeMirrorArray(wildcardTypeArguments);
        TypeElement abstractBuilderElement = env.elements()
                .getTypeElement("com.laynemobile.proxy.AbstractProxyHandlerBuilder");
        DeclaredType abstractBuilderType = env.types().getDeclaredType(abstractBuilderElement, proxyType);

        classBuilder.superclass(TypeName.get(abstractBuilderType))
                .addModifiers(Modifier.PUBLIC);
        // TODO: add annotation!
//                .addAnnotation(AnnotationSpec.builder(ProxyFunctionImplementation.class)
//                        .addMember("value", "$T.class", subclass)
//                        .build())
        ;

        List<TypeVariableAlias> typeVariables = Util.buildList(proxyElement.element().element().getTypeParameters(),
                new Transformer<TypeVariableAlias, TypeParameterElementAlias>() {
                    @Override
                    public TypeVariableAlias transform(TypeParameterElementAlias typeParameterElementAlias) {
                        TypeMirrorAlias type = typeParameterElementAlias.asType();
                        if (type.getKind() == TypeKind.TYPEVAR) {
                            return AliasTypes.get((TypeVariable) type.actual());
                        }
                        return null;
                    }
                });

        List<TypeVariableName> typeVariableNames
                = buildList(typeVariables, new Transformer<TypeVariableName, TypeVariableAlias>() {
            @Override public TypeVariableName transform(TypeVariableAlias typeVariableAlias) {
                return TypeVariableName.get(typeVariableAlias.actual());
            }
        });
        classBuilder.addTypeVariables(typeVariableNames);

        TypeName outputType;
        TypeName[] typeNameArray = typeNameArray(typeVariableNames);
        if (typeNameArray.length == 0) {
            outputType = typeName();
        } else {
            outputType = ParameterizedTypeName.get(typeName(), typeNameArray);
        }

        int size = functions.size();
        Set<String> names = new HashSet<>(size);
        Map<String, Integer> duplicateNames = new HashMap<>(size);
        for (ProxyFunctionOutput function : functions) {
            String name = function.element().name();
            if (names.contains(name)) {
                duplicateNames.put(name, 1);
            } else {
                names.add(name);
            }
        }

        Set<FieldSpec> handlerFields = new LinkedHashSet<>(size);
        for (ProxyFunctionOutput function : functions) {
            ProxyFunctionTypeOutputStub typeOutputStub = function.typeOutputStub();
            ProxyFunctionElement element = function.element();
            String fieldName;
            String fieldParamName = element.name();
            Integer count = duplicateNames.get(fieldParamName);
            if (count == null) {
                fieldName = fieldParamName;
            } else {
                // Find method part of name in typeOutputStub
                String className = typeOutputStub.className();
                int index;
                if ((index = className.indexOf('_')) != -1) {
                    fieldName = className.substring(index + 1);
                } else {
                    fieldName = fieldParamName + count;
                    duplicateNames.put(fieldParamName, count + 1);
                }
            }
            String methodName = "set" +
                    fieldName.substring(0, 1).toUpperCase(Locale.US) +
                    fieldName.substring(1);

            TypeElement fieldElement = typeOutputStub.element(env);
            DeclaredType fieldType = env.types().getDeclaredType(fieldElement, typeParams);
            DeclaredType wildcardFieldType = env.types().getDeclaredType(fieldElement, wildcardTypeParams);
            TypeName wildcardFieldTypeName = TypeName.get(wildcardFieldType);

            // create field
            FieldSpec fieldSpec = FieldSpec.builder(wildcardFieldTypeName, fieldName)
                    .addModifiers(Modifier.PRIVATE)
                    .build();
            classBuilder.addField(fieldSpec);
            handlerFields.add(fieldSpec);

            // create setter method for field
            classBuilder.addMethod(MethodSpec.methodBuilder(methodName)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(outputType)
                    .addParameter(wildcardFieldTypeName, fieldParamName)
                    .addStatement("this.$N = $L", fieldSpec, fieldParamName)
                    .addStatement("return this")
                    .build());

            // create method for each constructor
            for (ExecutableElement constructor : ElementFilter.constructorsIn(fieldElement.getEnclosedElements())) {
                List<? extends VariableElement> params = constructor.getParameters();
                MethodSpec.Builder method = MethodSpec.methodBuilder(methodName)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(outputType);
                if (params.size() == 0) {
                    method.addStatement("this.$N = new $T()", fieldSpec, fieldType);
                } else if (params.size() == 1) {
                    VariableElement param = params.get(0);
                    TypeName paramType = TypeName.get(param.asType());
                    method.addParameter(paramType, fieldParamName)
                            .addStatement("this.$N = new $T($L)", fieldSpec, fieldType, fieldParamName);
                } else {
                    List<String> paramNames = new ArrayList<>();
                    for (VariableElement parameter : params) {
                        TypeMirror paramType = parameter.asType();
                        String paramName = parameter.getSimpleName().toString();
                        paramNames.add(paramName);
                        method.addParameter(TypeName.get(paramType), paramName);
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

                    method.addStatement("this.$N = new $T($L)", fieldSpec, fieldType, paramString.toString());
                }

                classBuilder.addMethod(method
                        .addStatement("return this")
                        .build());
            }
        }

        TypeElement typeTokenElement = env.elements().getTypeElement("com.laynemobile.proxy.TypeToken");
        DeclaredType typeTokenType = env.types().getDeclaredType(typeTokenElement, proxyType);

        // build function
        TypeElement proxyHandlerElement = env.elements().getTypeElement("com.laynemobile.proxy.ProxyHandler");
        DeclaredType proxyHandlerType = env.types().getDeclaredType(proxyHandlerElement, proxyType);
        MethodSpec.Builder proxyHandlerMethod = MethodSpec.methodBuilder("proxyHandler")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(TypeName.get(proxyHandlerType));

        CodeBlock.Builder returnCode = CodeBlock.builder()
                .add("$[")
                .add("return $T.builder(new $T() {})\n",
                        ClassName.get(proxyHandlerElement), TypeName.get(typeTokenType));

        for (FieldSpec handlerField : handlerFields) {
            returnCode.add(".handle(handler($N))\n", handlerField);
        }

        proxyHandlerMethod.addCode(returnCode
                .add(".build()")
                .add(";\n$]")
                .build());
        return classBuilder.addMethod(proxyHandlerMethod.build())
                .build();
    }
}
