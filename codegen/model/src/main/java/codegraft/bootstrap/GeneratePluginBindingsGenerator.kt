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

package codegraft.bootstrap

import com.google.auto.common.MoreElements
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import com.squareup.javapoet.WildcardTypeName
import dagger.MapKey
import dagger.Module
import dagger.internal.codegen.uniqueScope
import dagger.model.Scope
import dagger.multibindings.Multibinds
import sourcerer.JavaOutput
import sourcerer.KotlinOutput
import sourcerer.Outputs
import sourcerer.interfaceBuilder
import sourcerer.name
import sourcerer.typeSpec
import java.io.Writer
import java.lang.annotation.Documented
import java.lang.annotation.RetentionPolicy
import javax.inject.Inject
import javax.lang.model.element.Modifier.ABSTRACT
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.STATIC

/*

@Module
interface ${Type}Module {
    @Multibinds
    $Scope
    fun bind$Types(): ${Type}Map
}

 */
class GeneratePluginBindingsGenerator(
    private val descriptor: GeneratePluginBindingsDescriptor
) {
    private val pluginType = descriptor.pluginType
    private val pluginTypeClassName: ClassName = ClassName.get(pluginType)
    private val pluginTypeName: String = pluginTypeClassName.name
    private val scope: Scope? = descriptor.element.uniqueScope

    private val keyType = pluginTypeClassName.wrapClassSubtype()
    private val valueType = ClassName.get(pluginType)
    private val mapType: TypeName by lazy {
        val rawMapType = ClassName.get(Map::class.java)
        ParameterizedTypeName.get(rawMapType, keyType, valueType)
    }

    private
    val packageName: String = pluginTypeClassName.packageName()

    fun process(): Outputs {
        return listOf(
            TypeMapKeyGenerator(),
            TypeModuleCreator(),
            TypeAliasGenerator()
        )
    }

    inner
    class TypeMapKeyGenerator : JavaOutput(
        rawType = ClassName.get(packageName, "${pluginTypeName}Key")
    ) {
        override
        fun classBuilder() =
            TypeSpec.annotationBuilder(name)!!

        override
        fun typeSpec() = typeSpec {
            addModifiers(PUBLIC, STATIC)

            addAnnotation(MapKey::class.java)
            addAnnotation(Documented::class.java)
            addAnnotation(AnnotationSpec.builder(java.lang.annotation.Retention::class.java).run {
                addMember(
                    "value",
                    "\$T.\$L", RetentionPolicy::class.java, RetentionPolicy.RUNTIME.name
                )
                build()
            })

            addMethod(MethodSpec.methodBuilder("value").run {
                addModifiers(PUBLIC, ABSTRACT)
                returns(keyType)
                build()
            })
        }
    }

    inner
    class TypeModuleCreator : JavaOutput(
        rawType = ClassName.get(packageName, "${pluginTypeName}Module")
    ) {
        override
        fun classBuilder() = outKlass.interfaceBuilder()

        override
        fun typeSpec() = typeSpec {
            addModifiers(PUBLIC, STATIC)
            addAnnotation(Module::class.java)

            val pluralName = descriptor.pluralName.capitalize()
            addMethod(MethodSpec.methodBuilder("bind$pluralName").run {
                addModifiers(PUBLIC, ABSTRACT)
                addAnnotation(Multibinds::class.java)
                scope?.let {
                    addAnnotation(AnnotationSpec.get(it.scopeAnnotation()))
                }
                returns(mapType)
                build()
            })
        }
    }

    inner
    class TypeAliasGenerator : KotlinOutput(
        packageName = packageName,
        fileName = "${pluginTypeName}_Alias"
    ) {
        override
        fun writeTo(writer: Writer) {
            val src = pluginSrc(
                packageName = packageName,
                Type = pluginTypeName,
                Types = descriptor.pluralName,
                Scope = scope.annotationName
            )
            writer.write(src)
        }
    }

    class Factory
    @Inject constructor(

    ) {
        fun create(
            descriptor: GeneratePluginBindingsDescriptor
        ): GeneratePluginBindingsGenerator {
            return GeneratePluginBindingsGenerator(descriptor)
        }
    }
}

fun ClassName.wrapClassSubtype(): ParameterizedTypeName {
    return wrapWildcardSubtype(ClassName.get(Class::class.java))
}

fun ClassName.wrapWildcardSubtype(
    wrapper: ClassName
): ParameterizedTypeName {
    val wildType = WildcardTypeName.subtypeOf(this)
    return ParameterizedTypeName.get(wrapper, wildType)
}

val Scope?.annotationName: String
    get() {
        return if (this == null) {
            ""
        } else {
            val annotationType = scopeAnnotation().annotationType
            val annotationElement = MoreElements.asType(annotationType.asElement())
            "@${annotationElement.qualifiedName}"
        }
    }

fun pluginSrc(
    packageName: String,
    Type: String,
    Types: String = Type + "s",
    Scope: String = "@Singleton"
): String = """
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

//
// Generated
//
package $packageName

import codegraft.inject.ClassKeyProviderMap
import codegraft.inject.ClassMap
import codegraft.inject.ClassProviderMap
import dagger.MapKey
import dagger.Module
import dagger.multibindings.Multibinds
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.reflect.KClass

typealias ${Type}Map = ClassMap<$Type>
typealias ${Type}ProviderMap = ClassProviderMap<$Type>

$Scope
class $Types
@Inject constructor(
    override val providers: ${Type}ProviderMap
) : ClassKeyProviderMap<$Type>()

""".trimIndent()

fun aliasSrc(
    packageName: String,
    Type: String
): String = """
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

//
// Generated
//
package $packageName

import codegraft.inject.ClassKeyProviderMap
import codegraft.inject.ClassMap

typealias ${Type}Map = ClassMap<$Type>
typealias ${Type}ProviderMap = ClassProviderMap<$Type>

""".trimIndent()
