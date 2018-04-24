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

import com.laynemobile.proxy.annotations.GenerateProcessorBuilder;
import com.laynemobile.proxy.internal.ProxyLog;
import com.laynemobile.proxy.processor.ProcessorHandler;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static com.laynemobile.proxy.Constants.Override;
import static com.laynemobile.proxy.Constants.SourceModule;

final class ProxyProcessorBuilder {
    private static final String TAG = ProxyProcessorBuilder.class.getSimpleName();

    private final TypeElement api;
    private final Elements elementUtils;
    private final Types typeUtils;

    private final ClassName className;
    private final TypeName typeName;
    final Util.ContainerType baseApiType;
    private final TypeSpec.Builder classBuilder;
    final List<TypeName> superTypeParams;

    // Builder Info
    final ClassName builderClassName;
    final TypeName builderTypeName;

    ProxyProcessorBuilder(TypeElement api, Elements elementUtils, Types typeUtils) {
        this.api = api;
        this.elementUtils = elementUtils;
        this.typeUtils = typeUtils;

        List<TypeVariableName> typeParams = Util.parseTypeParams(api);
        this.className = ClassName.get(api);
        this.builderClassName = ClassName.get(className.packageName(), className.simpleName() + "Builder");
        this.typeName = Util.paramType(className, typeParams);
        this.builderTypeName = Util.paramType(builderClassName, typeParams);
        this.baseApiType = Util.parseBaseApiType(api, typeUtils);
        this.superTypeParams = Collections.unmodifiableList(baseApiType.typeArguments);
        this.classBuilder = TypeSpec.classBuilder(builderClassName.simpleName())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(Util.builder(typeName))
                .addTypeVariables(typeParams)
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .build());

        ProxyLog.d(TAG, "superTypeParams: %s", superTypeParams);
        ParameterizedTypeName sourceBuilder = Util.sourceBuilder(superTypeParams);
        FieldSpec builderField = FieldSpec.builder(sourceBuilder, "builder")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .initializer("new $T()", sourceBuilder)
                .build();
        classBuilder.addField(builderField);
    }

    void writeTo(Filer filer) throws IOException {
        JavaFile javaFile = build();
//        javaFile.writeTo(System.out);
        javaFile.writeTo(filer);
    }

    private JavaFile build() {
        Set<ClassName> sourceTypes = new HashSet<>();
        ProxyLog.d(TAG, "GenerateApiBuilder element: %s", api.getQualifiedName());
        GenerateProcessorBuilder annotation = api.getAnnotation(GenerateProcessorBuilder.class);

        try {
            Class<? extends Builder<? extends ProcessorHandler.Parent<?, ?, ?>>> parent = annotation.value();
            throw new IllegalStateException(
                    "should throw MirroredTypesException. Not sure what do do if not! " + parent);
        } catch (MirroredTypeException e) {
            TypeMirror mirror = e.getTypeMirror();
            // Parent type TODO:
            TypeElement source = (TypeElement) typeUtils.asElement(mirror);
            ModuleBuilder moduleBuilder = new ModuleBuilder(this, source, elementUtils, typeUtils);
            ClassName sourceType = moduleBuilder.getSourceType();
            if (sourceTypes.contains(sourceType)) {
                throw new IllegalStateException(
                        "Cannot add module '" + source.getQualifiedName() + "' because source type " +
                                "'" + sourceType + "' is already defined for another module in '" +
                                api.getQualifiedName() + "'");
            }
            sourceTypes.add(sourceType);
            moduleBuilder.writeTo(classBuilder);
        }

        try {
            Class<? extends Builder<? extends ProcessorHandler<?, ?, ?>>>[] builders = annotation.extensions();
            throw new IllegalStateException("should throw MirroredTypesException. Not sure what do do if not! " +
                    Arrays.toString(builders));
        } catch (MirroredTypesException e) {
            for (TypeMirror mirror : e.getTypeMirrors()) {
                TypeElement source = (TypeElement) typeUtils.asElement(mirror);
                ModuleBuilder moduleBuilder = new ModuleBuilder(this, source, elementUtils, typeUtils);
                ClassName sourceType = moduleBuilder.getSourceType();
                if (sourceTypes.contains(sourceType)) {
                    throw new IllegalStateException(
                            "Cannot add module '" + source.getQualifiedName() + "' because source type " +
                                    "'" + sourceType + "' is already defined for another module in '" +
                                    api.getQualifiedName() + "'");
                }
                sourceTypes.add(sourceType);
                moduleBuilder.writeTo(classBuilder);
            }
        }

        // Module method
        ParameterSpec param = ParameterSpec.builder(SourceModule, "module")
                .build();
        MethodSpec addModule = MethodSpec.methodBuilder("addModule")
                .addModifiers(Modifier.PUBLIC)
                .returns(this.builderTypeName)
                .addParameter(param)
                .addStatement("builder.module($N)", param)
                .addStatement("return this")
                .build();
        classBuilder.addMethod(addModule);

        // Modules method
        param = ParameterSpec.builder(ArrayTypeName.of(SourceModule), "modules")
                .build();
        MethodSpec addModules = MethodSpec.methodBuilder("addModules")
                .varargs()
                .addModifiers(Modifier.PUBLIC)
                .returns(this.builderTypeName)
                .addParameter(param)
                .addStatement("builder.modules($N)", param)
                .addStatement("return this")
                .build();
        classBuilder.addMethod(addModules);

        // Module List method
        param = ParameterSpec.builder(Util.list(SourceModule), "modules")
                .build();
        MethodSpec addModuleList = MethodSpec.methodBuilder("addModules")
                .addModifiers(Modifier.PUBLIC)
                .returns(this.builderTypeName)
                .addParameter(param)
                .addStatement("builder.modules($N)", param)
                .addStatement("return this")
                .build();
        classBuilder.addMethod(addModuleList);

        // Source method
        TypeName sourceType = Util.source(superTypeParams);
        MethodSpec source = MethodSpec.methodBuilder("source")
                .addModifiers(Modifier.PUBLIC)
                .returns(sourceType)
                .addStatement("return builder.build()")
                .build();
        classBuilder.addMethod(source);

        // RequestProcessor method
        TypeName requestProcessorType = Util.requestProcessor(superTypeParams);
        TypeName requestProcessorBuilderType = Util.requestProcessorBuilder(superTypeParams);
        MethodSpec requestProcessor = MethodSpec.methodBuilder("requestProcessor")
                .addModifiers(Modifier.PUBLIC)
                .returns(requestProcessorType)
                .addCode(CodeBlock.builder()
                        .add("return new $T()\n", requestProcessorBuilderType)
                        .indent().indent().indent().indent()
                        .add(".setSource($N())\n", source)
                        .addStatement(".build()")
                        .unindent().unindent().unindent().unindent()
                        .build())
                .build();
        classBuilder.addMethod(requestProcessor);

        // Build method
        MethodSpec build = MethodSpec.methodBuilder("build")
                .addAnnotation(Override)
                .addModifiers(Modifier.PUBLIC)
                .returns(typeName)
                .addStatement("return new $T($N())", typeName, requestProcessor)
                .build();
        classBuilder.addMethod(build);

        return JavaFile.builder(className.packageName(), classBuilder.build())
                .build();
    }
}
