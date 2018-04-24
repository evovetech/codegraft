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

package com.laynemobile.proxy.model.output;

import com.laynemobile.proxy.Util;
import com.laynemobile.proxy.Util.Transformer;
import com.laynemobile.proxy.elements.TypeParameterElementAlias;
import com.laynemobile.proxy.model.ProxyEnv;
import com.laynemobile.proxy.model.ProxyFunctionElement;
import com.laynemobile.proxy.types.AliasTypes;
import com.laynemobile.proxy.types.TypeMirrorAlias;
import com.laynemobile.proxy.types.TypeVariableAlias;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;

import sourcerer.processor.Env;

import static com.laynemobile.proxy.Util.buildList;
import static com.laynemobile.proxy.Util.typeMirrorArray;
import static javax.lang.model.util.ElementFilter.constructorsIn;

public class ProxyFunctionTypeOutputStub extends DefaultTypeElementOutputStub {
    private final Env env;
    private final ProxyFunctionAbstractTypeOutputStub parentOutput;
    private final DeclaredType superClass;

    ProxyFunctionTypeOutputStub(ProxyFunctionAbstractTypeOutputStub parentOutput, Env env) {
        this(parentOutput, (DeclaredType) parentOutput.element(env).asType(), env);
    }

    ProxyFunctionTypeOutputStub(ProxyFunctionAbstractTypeOutputStub parentOutput, DeclaredType superClass, Env env) {
        super(parentOutput.basePackageName(), parentOutput.baseClassName());
        this.parentOutput = parentOutput;
        this.superClass = superClass;
        this.env = ProxyEnv.wrap(env);
    }

    @Override protected TypeSpec build(TypeSpec.Builder classBuilder) {
        classBuilder = classBuilder.superclass(TypeName.get(superClass))
                .addModifiers(Modifier.PUBLIC);

        ProxyFunctionAbstractTypeOutputStub stub = parentOutput;
        List<TypeVariableAlias> typeVariables = Util.buildList(stub.parent().element().element().getTypeParameters(),
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

        for (TypeVariableAlias typeVariable : typeVariables) {
            classBuilder.addTypeVariable(TypeVariableName.get(typeVariable.actual()));
        }

        ProxyFunctionElement function = stub.function();
        String name = function.name();

        List<? extends TypeMirror> superTypeArguments = superClass.getTypeArguments();
        env.log("superTypeArguments: %s", superTypeArguments);
        List<TypeVariable> superTypeVarArguments
                = buildList(superTypeArguments, new Transformer<TypeVariable, TypeMirror>() {
            @Override public TypeVariable transform(TypeMirror typeMirror) {
                if (typeMirror.getKind() == TypeKind.TYPEVAR) {
                    return (TypeVariable) typeMirror;
                }
                return null;
            }
        });
        env.log("superTypeVarArguments: %s", superTypeVarArguments);
        List<DeclaredType> superDeclaredTypeArguments
                = buildList(superTypeArguments, new Transformer<DeclaredType, TypeMirror>() {
            @Override public DeclaredType transform(TypeMirror typeMirror) {
                if (typeMirror.getKind() == TypeKind.DECLARED) {
                    return (DeclaredType) typeMirror;
                }
                return null;
            }
        });
        env.log("superDeclaredTypeArguments: %s", superDeclaredTypeArguments);

        // Constructor
        for (ExecutableElement constructor : constructorsIn(superClass.asElement().getEnclosedElements())) {
            MethodSpec.Builder method = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC);

            Set<String> paramNames = new HashSet<>();
            for (VariableElement parameter : constructor.getParameters()) {
                TypeMirror paramType = parameter.asType();
                if (paramType.getKind() == TypeKind.DECLARED) {
                    DeclaredType declaredParamType = (DeclaredType) paramType;
                    if (!declaredParamType.getTypeArguments().isEmpty()) {
                        TypeMirror[] typeArguments
                                = paramTypes(env, declaredParamType, superTypeVarArguments, superDeclaredTypeArguments);
                        TypeElement paramTypeElement = (TypeElement) declaredParamType.asElement();
                        paramType = env.types().getDeclaredType(paramTypeElement, typeArguments);
                    }
                }
                String paramName = parameter.getSimpleName().toString();
                paramNames.add(paramName);
                method.addParameter(TypeName.get(paramType), paramName);
            }

            boolean first = true;
            StringBuilder params = new StringBuilder();
            for (String paramName : paramNames) {
                if (!first) {
                    params.append(", ");
                }
                first = false;
                params.append(paramName);
            }

            method.addStatement("super($L)", params.toString());

            classBuilder.addMethod(method.build());
        }
//        classBuilder.addMethod(MethodSpec.constructorBuilder()
//                .addModifiers(Modifier.PUBLIC)
//                .addParameter(TypeName.get(function.functionType()), name)
//                .addStatement("super($L)", name)
//                .build());

        return classBuilder.build();
    }

    private static TypeMirror[] paramTypes(Env env, DeclaredType declaredParamType,
            List<TypeVariable> superTypeVarArguments, List<DeclaredType> superDeclaredTypeArguments) {

        superDeclaredTypeArguments = new ArrayList<>(superDeclaredTypeArguments);
        TypeMirror[] typeArguments = typeMirrorArray(declaredParamType.getTypeArguments());
        int length = typeArguments.length;

        FOR:
        for (int i = 0; i < length; i++) {
            TypeMirror type = typeArguments[i];
            if (type.getKind() == TypeKind.TYPEVAR) {
                TypeVariable typeVar = (TypeVariable) type;
                Element typeVarElement = typeVar.asElement();
                for (TypeVariable superTypeVar : superTypeVarArguments) {
                    Element superTypeVarElement = superTypeVar.asElement();
                    if (superTypeVarElement.getSimpleName().toString()
                            .equals(typeVarElement.getSimpleName().toString())
//                                            && typeVar.getUpperBound().equals(superTypeVar.getUpperBound())
                            ) {
                        env.log("found type var: %s", superTypeVar);
                        continue FOR;
                    }
                }

                TypeMirror upperBound = typeVar.getUpperBound();
                TypeMirrorAlias upperBoundAlias = AliasTypes.get(upperBound);
                env.log("looking for typeVar: %s, upperBound: %s", typeVar, upperBound);

                Iterator<DeclaredType> it = superDeclaredTypeArguments.iterator();
                WHILE:
                while (it.hasNext()) {
                    DeclaredType superDeclaredTypeArgument = it.next();
                    env.log("checking declared type: %s", superDeclaredTypeArgument);
                    if (AliasTypes.get(superDeclaredTypeArgument).equals(upperBoundAlias)) {
                        env.log("found type var: %s=%s", typeVar, superDeclaredTypeArgument);
                        typeArguments[i] = superDeclaredTypeArgument;
                        it.remove();
                        continue WHILE;
                    }

                    if (env.types().isAssignable(upperBound, superDeclaredTypeArgument)) {
                        env.log("found type var: %s=%s", typeVar, superDeclaredTypeArgument);
                        typeArguments[i] = superDeclaredTypeArgument;
                        it.remove();
                        continue WHILE;
                    }

                    for (TypeMirror superType : env.types().directSupertypes(superDeclaredTypeArgument)) {
                        env.log("direct super type: %s", superType);
                        if (AliasTypes.get(superType).equals(upperBoundAlias)) {
                            env.log("found type var: %s=%s", typeVar, superDeclaredTypeArgument);
                            typeArguments[i] = superDeclaredTypeArgument;
                            it.remove();
                            continue WHILE;
                        }
                    }
                }
            }
        }
        return typeArguments;
    }
}
