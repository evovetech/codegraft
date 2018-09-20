/*
 * Copyright (C) 2018 evove.tech
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package sourcerer

import com.squareup.javapoet.AnnotationSpec
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

fun AnnotationSpec.Builder.addTo(name: String): (TypeName) -> Unit = {
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
