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

import com.laynemobile.proxy.annotations.Generated;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import sourcerer.processor.Env;

public abstract class AbstractTypeElementOutputStub<T extends TypeElementOutput> extends DefaultTypeElementStub implements TypeElementOutputStub {
    protected AbstractTypeElementOutputStub(String packageName, String className) {
        super(packageName, className);
    }

    protected TypeSpec build(TypeSpec.Builder classBuilder) {
        throw new UnsupportedOperationException("must implement for default writeTo(env) implementation");
    }

    protected TypeSpec newTypeSpec() {
        return build(TypeSpec.classBuilder(className())
                .addAnnotation(Generated.class)
        );
    }

    protected abstract T convert(TypeElementOutput output);

    protected T create(TypeSpec typeSpec, boolean didWrite) {
        return convert(AbstractTypeElementOutput.create(this, typeSpec, didWrite));
    }

    protected T write(Env env) throws IOException {
        TypeSpec typeSpec = newTypeSpec();
        JavaFile javaFile = JavaFile.builder(packageName(), typeSpec)
                .build();
        env.log("writing %s -> \n%s", qualifiedName(), javaFile.toString());
        javaFile.writeTo(env.filer());
        return create(typeSpec, true);
    }

    @Override public final T writeTo(Env env) throws IOException {
        TypeElement typeElement = env.elements().getTypeElement(qualifiedName());
        env.log("potentially writing type element: %s", typeElement);
        if (typeElement != null) {
            TypeMirror typeMirror = typeElement.asType();
            env.log("asType()=%s, typeKind=%s", typeMirror, typeMirror.getKind());
            if (typeMirror.getKind() != TypeKind.ERROR) {
                env.log("found type element");
                return create(newTypeSpec(), false);
            }
        }
        return write(env);
    }
}
