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

import codegraft.inject.BootstrapComponent
import codegraft.inject.ClassKeyProviderMap
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import com.squareup.javapoet.WildcardTypeName
import dagger.MapKey
import dagger.Module
import dagger.internal.codegen.uniqueScope
import dagger.model.Scope
import dagger.multibindings.Multibinds
import org.jetbrains.annotations.NotNull
import sourcerer.JavaOutput
import sourcerer.KotlinOutput
import sourcerer.Outputs
import sourcerer.addTo
import sourcerer.interfaceBuilder
import sourcerer.name
import sourcerer.qualifiedName
import sourcerer.typeSpec
import java.io.Writer
import java.lang.annotation.Documented
import java.lang.annotation.RetentionPolicy
import javax.inject.Inject
import javax.inject.Provider
import javax.lang.model.element.Modifier.ABSTRACT
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.STATIC

val RawClassType = ClassName.get(Class::class.java)!!
val RawMapType = ClassName.get(Map::class.java)!!
val RawProviderType = ClassName.get(Provider::class.java)!!

class GeneratePluginBindingsGenerator(
    private val descriptor: GeneratePluginBindingsDescriptor
) {
    private val annotationType = descriptor.element
    private val annotationTypeClassName = ClassName.get(annotationType)
    private val pluginType = descriptor.pluginType
    private val pluginTypeClassName: ClassName = ClassName.get(pluginType)
    private val pluginTypeName: String = descriptor.pluginTypeName
    private val pluginMapTypeName: String = descriptor.pluginMapTypeName
    private val scope: Scope? = descriptor.element.uniqueScope

    private val keyType = pluginTypeClassName.wrapClassSubtype()
    private val valueType = ClassName.get(pluginType)
    private val valueProviderType = ParameterizedTypeName.get(RawProviderType, valueType)
    private val mapType: TypeName by lazy {
        ParameterizedTypeName.get(RawMapType, keyType, valueType)
    }
    private val mapProviderType: TypeName by lazy {
        val rawMapType = ClassName.get(Map::class.java)
        ParameterizedTypeName.get(rawMapType, keyType, valueProviderType)
    }

    private
    val packageName: String = annotationTypeClassName.packageName()

    fun process(): Outputs {
        val typeModuleGenerator = TypeModuleGenerator()
        val typeMapGenerator = TypeMapGenerator()
        val typeComponentGenerator = TypeComponentGenerator(
            typeModuleGenerator,
            typeMapGenerator
        )
        return listOf(
            TypeAliasGenerator(),
            TypeMapKeyGenerator(),
            typeModuleGenerator,
            typeMapGenerator,
            typeComponentGenerator
        )
    }

    inner
    class TypeAliasGenerator : KotlinOutput(
        packageName = packageName,
        fileName = "${pluginTypeName}_Alias"
    ) {
        override
        fun writeTo(writer: Writer) {
            val src = aliasSrc(
                packageName = packageName,
                typeName = pluginTypeClassName,
                typeAliasName = pluginTypeName
            )
            writer.write(src)
        }
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
    class TypeModuleGenerator : JavaOutput(
        rawType = ClassName.get(packageName, "${pluginTypeName}Module")
    ) {
        override
        fun classBuilder() = outKlass.interfaceBuilder()

        override
        fun typeSpec() = typeSpec {
            addModifiers(PUBLIC, STATIC)
            addAnnotation(Module::class.java)

            addMethod(MethodSpec.methodBuilder("bind$pluginMapTypeName").run {
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
    class TypeMapGenerator : JavaOutput(
        rawType = ClassName.get(packageName, pluginMapTypeName)
    ) {
        val rawSuperType = ClassName.get(ClassKeyProviderMap::class.java)
        val superType = ParameterizedTypeName.get(rawSuperType, pluginTypeClassName)

        override
        fun typeSpec() = typeSpec {
            addModifiers(PUBLIC, FINAL)
            superclass(superType)

            scope?.let {
                addAnnotation(AnnotationSpec.get(it.scopeAnnotation()))
            }

            // constructor
            addMethod(MethodSpec.constructorBuilder().run {
                addAnnotation(Inject::class.java)
                val param = ParameterSpec.builder(mapProviderType, "providers")
                        .addAnnotation(NotNull::class.java)
                        .build()
                addParameter(param)
                addStatement("super(\$N)", param)
                build()
            })
        }
    }

    inner
    class TypeComponentGenerator(
        val typeModuleGenerator: TypeModuleGenerator,
        val typeMapGenerator: TypeMapGenerator
    ) : JavaOutput(
        rawType = ClassName.get(packageName, "${pluginTypeName}Component")
    ) {
        override
        fun classBuilder() = outKlass.interfaceBuilder()

        override
        fun typeSpec() = typeSpec {
            addModifiers(PUBLIC, STATIC)
            addAnnotation(AnnotationSpec.builder(BootstrapComponent::class.java).run {
                typeModuleGenerator.outKlass.rawType
                        .let(addTo("applicationModules"))
                addMember("autoInclude", "\$L", false)
                addMember("flatten", "\$L", descriptor.flattenComponent)
                build()
            })

            addMethod(MethodSpec.methodBuilder("get$pluginMapTypeName").run {
                addModifiers(PUBLIC, ABSTRACT)
                returns(typeMapGenerator.outKlass.rawType)
                build()
            })
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
    return wrapWildcardSubtype(RawClassType)
}

fun ClassName.wrapWildcardSubtype(
    wrapper: ClassName
): ParameterizedTypeName {
    val wildType = WildcardTypeName.subtypeOf(this)
    return ParameterizedTypeName.get(wrapper, wildType)
}

fun aliasSrc(
    packageName: String,
    typeName: ClassName,
    typeAliasName: String = typeName.name
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

import codegraft.inject.ClassMap
import codegraft.inject.ClassProviderMap
import ${typeName.qualifiedName}

typealias ${typeAliasName}Map = ClassMap<${typeName.name}>
typealias ${typeAliasName}ProviderMap = ClassProviderMap<${typeName.name}>

""".trimIndent()