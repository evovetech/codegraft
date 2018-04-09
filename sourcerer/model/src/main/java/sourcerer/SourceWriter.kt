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

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import javax.annotation.processing.Filer
import javax.lang.model.element.Modifier

/**
 * Created by layne on 3/8/18.
 */
interface SourceWriter {
    val outKlass: Klass

    fun newBuilder(): TypeSpec.Builder
    fun typeSpec(): TypeSpec

    fun javaFile() = JavaFile.builder(outKlass.pkg.name, typeSpec())
            .build()!!

    fun writeTo(filer: Filer) = javaFile()
            .writeTo(filer)
}

fun AnnotationSpec.Builder.addTo(name: String): (ClassName) -> Unit = {
    addMember(name, "\$T.class", it)
}

fun AnnotationSpec.Builder.addKlassTo(name: String): (Klass) -> Unit = {
    addTo(name)(it.rawType)
}

inline fun SourceWriter.typeSpec(
    init: TypeSpec.Builder.() -> Unit
): TypeSpec {
    val builder = newBuilder()
    builder.apply(init)
    return builder.build()
}

fun TypeSpec.Builder.addAnnotation(
    klass: Klass
) = addAnnotation(klass.rawType)!!

fun TypeSpec.Builder.addAnnotation(
    klass: Klass,
    init: AnnotationSpec.Builder.() -> Unit
) = addAnnotation(klass.annotation(init))!!

inline fun TypeSpec.Builder.addMethod(
    name: String,
    vararg modifiers: Modifier,
    init: MethodSpec.Builder.() -> Unit
) {
    val builder = MethodSpec.methodBuilder(name)
    builder.addModifiers(*modifiers)
    builder.init()
    addMethod(builder.build())
}

fun TypeSpec.Builder.addSuperinterfaces(
    vararg types: TypeName
) = addSuperinterfaces(types.asList())!!

inline
fun <reified A : Annotation> TypeSpec.Builder.intoCollection() {
    addAnnotation(Codegen.Inject.IntoCollection) {
        addTo("value")(ClassName.get(A::class.java))
    }
}
