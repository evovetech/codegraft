/*
 * Copyright 2018 evove.tech
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

package sourcerer

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import java.io.File
import java.io.IOException
import javax.annotation.processing.Filer
import javax.lang.model.element.Modifier

class SourceWriter internal constructor(sourcerer: Extension.Sourcerer) {
    private val ext: Extension
    private val classBuilder: TypeSpec.Builder

    init {
        this.ext = sourcerer.extension()
        this.classBuilder = TypeSpec.classBuilder(ext.className())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)

        val kind = ext.kind()

        // Constructor
        val constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
        if (kind == ExtensionClass.Kind.StaticDelegate) {
            val exception = ClassName.get(AssertionError::class.java)
            constructor.addStatement("throw new \$T(\$S)", exception, "no instances")
        }
        classBuilder.addMethod(constructor.build())

        if (kind != ExtensionClass.Kind.StaticDelegate) {
            // Instance field
            val instanceType = ext.typeName()
            val instanceField = FieldSpec.builder(
                instanceType, "INSTANCE", Modifier.PRIVATE, Modifier.STATIC,
                Modifier.FINAL
            )
                    .initializer("new \$T()", instanceType)
                    .build()
            classBuilder.addField(instanceField)

            // Instance Method
            val instanceMethod = MethodSpec.methodBuilder("getInstance")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addStatement("return INSTANCE")
                    .returns(instanceType)
                    .build()
            classBuilder.addMethod(instanceMethod)
        }

        classBuilder.addMethods(sourcerer.methods())
    }

    @Throws(IOException::class)
    fun writeTo(filer: Filer) {
        javaFile().writeTo(filer)
    }

    @Throws(IOException::class)
    fun writeTo(outputDir: File) {
        javaFile().writeTo(outputDir)
    }

    private fun javaFile(): JavaFile {
        // Write java file
        return JavaFile.builder(ext.packageName(), classBuilder.build())
                .build()
    }
}
