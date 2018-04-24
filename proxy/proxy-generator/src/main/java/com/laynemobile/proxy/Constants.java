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

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.WildcardTypeName;

import java.util.List;

final class Constants {
    static final String PACKAGE = "com.laynemobile.proxy";
    static final ClassName Override = ClassName.get(Override.class);
    static final ClassName Source = ClassName.get(PACKAGE, "Source");
    static final ClassName TypeHandler = ClassName.get(ProxyHandler.class);
    static final TypeName SourceModule
            = ParameterizedTypeName.get(TypeHandler, WildcardTypeName.subtypeOf(Source));
    static final ClassName Builder = ClassName.get(Builder.class);
    static final ClassName SourceBuilder = ClassName.get(PACKAGE, "SourceBuilder");
    static final ClassName RequestProcessor = ClassName.get(PACKAGE, "RequestProcessor");
    static final ClassName RequestProcessorBuilder = ClassName.get(RequestProcessor.packageName(),
            RequestProcessor.simpleName(), "Builder");
    static final ClassName List = ClassName.get(List.class);

    private Constants() { throw new AssertionError("no instances"); }
}
