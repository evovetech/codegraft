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

package codegraft.plugins

import codegraft.inject.BootstrapComponent
import codegraft.inject.ClassKeyProviderMap
import codegraft.packageName
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
import javax.lang.model.element.TypeElement

val RawClassType = ClassName.get(Class::class.java)!!
val RawMapType = ClassName.get(Map::class.java)!!
val RawProviderType = ClassName.get(Provider::class.java)!!

val TypeElement.className: ClassName
    get() = ClassName.get(this)

class GeneratePluginBindingsGenerator(
    val descriptor: GeneratePluginBindingsDescriptor
) {
    val packageName: String = descriptor.packageName
    val annotationType = descriptor.element
    val scope: Scope? = annotationType.uniqueScope
    val mapKeyAnnotationType: ClassName = descriptor.mapKeyAnnotationType

    private val annotation = descriptor.annotation
    private val pluginType = annotation.pluginType
    private val pluginTypeName = annotation.pluginTypeName
    private val pluginMapTypeName = annotation.pluginMapTypeName

    private val keyType = pluginType.className.wrapClassSubtype()
    private val valueType = pluginType.className
    private val valueProviderType = ParameterizedTypeName.get(RawProviderType, valueType)
    private val mapType: TypeName by lazy {
        ParameterizedTypeName.get(RawMapType, keyType, valueType)
    }
    private val mapProviderType: TypeName by lazy {
        ParameterizedTypeName.get(RawMapType, keyType, valueProviderType)
    }

    fun process(): Outputs {
        val typeModuleGenerator = TypeModuleGenerator()
        val typeMapGenerator = TypeMapGenerator()
        val typeComponentGenerator = TypeComponentGenerator(
            typeModuleGenerator,
            typeMapGenerator
        )
        return listOf(
            TypeAliasGenerator(),
            TypeMapKeyAnnotationGenerator(),
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
                typeName = pluginType.className,
                typeAliasName = pluginTypeName
            )
            writer.write(src)
        }
    }

    inner
    class TypeMapKeyAnnotationGenerator : JavaOutput(
        rawType = mapKeyAnnotationType
    ) {
        override
        fun classBuilder() =
            TypeSpec.annotationBuilder(outKlass.name)!!

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
        val superType = ParameterizedTypeName.get(rawSuperType, pluginType.className)

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
                addMember("flatten", "\$L", annotation.flattenComponent)
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
