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

package sourcerer.codegen

import com.google.auto.common.MoreTypes
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeName
import dagger.internal.codegen.Dependency
import sourcerer.join
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror

data
class MethodBuilder(
    val name: String,
    val init: MethodSpec.Builder.() -> Unit = {}
) {
    fun withName(name: String): MethodBuilder {
        return copy(name = name)
    }

    fun build(): MethodSpec {
        val builder = MethodSpec.methodBuilder(name)
        builder.init()
        return builder.build()
    }
}

fun Collection<MethodBuilder>.buildUnique() = groupBy {
    it.name
}.flatMap { (key, methods) ->
    val size = methods.size
    val builders = if (size > 1) {
        var i = 1
        methods.map {
            val n = "$key${i++}"
            it.withName(n)
        }
    } else {
        methods
    }
    builders.map { it.build() }
}

data
class ParamBuilder(
    val name: String,
    val type: TypeName,
    val init: ParameterSpec.Builder.() -> Unit = {}
) {
    fun withName(name: String): ParamBuilder {
        return copy(name = name)
    }

    fun build(): ParameterSpec {
        val builder = ParameterSpec.builder(type, name)
        builder.init()
        return builder.build()
    }
}

fun ClassName.buildParameter(
    init: ParameterSpec.Builder.() -> Unit = {}
): ParameterSpec {
    val name = simpleNames().join('_').decapitalize()
    return ParamBuilder(name, this, init)
            .build()
}

fun TypeElement.buildParameter(
    init: ParameterSpec.Builder.() -> Unit = {}
): ParameterSpec = ClassName.get(this)
        .buildParameter(init)

fun TypeMirror.buildParameter(
    name: String,
    init: ParameterSpec.Builder.() -> Unit = {}
): ParameterSpec {
    val type = TypeName.get(this)
    return ParamBuilder(name, type, init)
            .build()
}

fun DeclaredType.buildParameter(
    init: ParameterSpec.Builder.() -> Unit = {}
): ParameterSpec {
    val name = asElement().simpleName.toString().decapitalize()
    return buildParameter(name, init)
}

fun Dependency.buildParameter(
    init: ParameterSpec.Builder.() -> Unit = {}
): ParameterSpec {
    val type = MoreTypes.asDeclared(key.type)
    return type.buildParameter {
        key.qualifier?.let {
            addAnnotation(AnnotationSpec.get(it))
        }
        init()
    }
}
