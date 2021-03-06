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

package codegraft.android

import codegraft.android.AndroidInjectModuleDescriptor.Kind.Application
import codegraft.inject.android.ActivityScope
import codegraft.inject.android.AndroidApplication
import codegraft.inject.android.ApplicationKey
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.WildcardTypeName
import dagger.Binds
import dagger.android.AndroidInjector
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import sourcerer.Dagger
import sourcerer.JavaOutput
import sourcerer.Klass
import sourcerer.SourceWriter
import sourcerer.addAnnotation
import sourcerer.addMethod
import sourcerer.addTo
import sourcerer.classBuilder
import sourcerer.className
import sourcerer.interfaceBuilder
import sourcerer.name
import sourcerer.toKlass
import sourcerer.typeSpec
import javax.inject.Inject
import javax.lang.model.element.Modifier.ABSTRACT
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.STATIC
import kotlin.reflect.KClass

class AndroidInjectModuleGenerator(
    private val descriptor: AndroidInjectModuleDescriptor
) : JavaOutput(
    rawType = ClassName.get(descriptor.element),
    outExt = "Module"
) {
    val applicationType = rawType

    override
    fun newBuilder() = outKlass.interfaceBuilder()

    override
    fun typeSpec() = when (descriptor.kind) {
        Application -> applicationTypeSpec()
        else -> normalTypeSpec()
    }

    private fun normalTypeSpec() = typeSpec {
        addModifiers(PUBLIC, STATIC)
        addAnnotation(Dagger.Module) {
            descriptor.kind.moduleType.java
                    .let(ClassName::get)
                    .let(addTo("includes"))
        }
        addMethod("contribute${rawType.name}") {
            addAnnotation(AnnotationSpec.builder(ContributesAndroidInjector::class.java).run {
                descriptor.includes
                        .map(ClassName::get)
                        .map(addTo("modules"))
                build()
            })
            addAnnotation(ActivityScope::class.java)
            addModifiers(PUBLIC, ABSTRACT)
            returns(rawType)
        }
    }

    private
    fun applicationTypeSpec() = typeSpec {
        addModifiers(PUBLIC, STATIC)

        val subcomponent =
            Subcomponent(this@AndroidInjectModuleGenerator)

        addAnnotation(Dagger.Module) {
            subcomponent.fullType
                    .let(addTo("subcomponents"))
            val addToIncludes = addTo("includes")
            descriptor.includes
                    .map(ClassName::get)
                    .map(addToIncludes)
            descriptor.kind.moduleType.java
                    .let(ClassName::get)
                    .let(addToIncludes)
        }

        addMethod("bind${applicationType.name}InjectorFactory") {
            addModifiers(PUBLIC, ABSTRACT)
            addAnnotation(Binds::class.java)
            addAnnotation(IntoMap::class.java)
            addAnnotation(AnnotationSpec.builder(ApplicationKey::class.java).run {
                applicationType.let(addTo("value"))
                build()
            })

            val param = ParameterSpec.builder(subcomponent.builder.fullType, "builder")
                    .build()
            addParameter(param)
            returns(androidApplicationInjectoryFactory())
        }

        addType(subcomponent.typeSpec())
    }

    class Subcomponent(
        private val parent: AndroidInjectModuleGenerator
    ) : SourceWriter {
        override
        val outKlass: Klass = "Subcomponent".toKlass()
        val fullType = parent.outKlass.rawType.nestedClass(outKlass.name)
        val applicationType
            get() = parent.applicationType
        val builder = SubcomponentBuilder(this)

        override
        fun newBuilder() = outKlass.interfaceBuilder()

        override
        fun typeSpec() = typeSpec {
            addModifiers(PUBLIC, STATIC)
            addAnnotation(dagger.Subcomponent::class.java)
            addSuperinterface(androidInjectorType(parent.applicationType))
            addType(builder.typeSpec())
        }
    }

    class SubcomponentBuilder(
        private val parent: Subcomponent
    ) : JavaOutput.Builder() {
        val fullType = parent.fullType.nestedClass(outKlass.name)

        override
        fun newBuilder() = outKlass.classBuilder()

        override
        fun typeSpec() = typeSpec {
            addModifiers(PUBLIC, STATIC, ABSTRACT)
            addAnnotation(dagger.Subcomponent.Builder::class.java)
            superclass(androidInjectorBuilderType(parent.applicationType))

            addMethod("build") {
                addModifiers(PUBLIC, ABSTRACT)
                returns(parent.fullType)
            }
        }
    }

    class Factory
    @Inject constructor(

    ) {
        fun create(
            descriptor: AndroidInjectModuleDescriptor
        ): AndroidInjectModuleGenerator {
            return AndroidInjectModuleGenerator(descriptor)
        }
    }
}

fun androidApplicationInjectoryFactory(): ParameterizedTypeName {
    return androidInjectorFactoryType(AndroidApplication::class)
}

fun androidInjectorFactoryType(klass: KClass<*>): ParameterizedTypeName {
    val rawType = AndroidInjector.Factory::class.className
    val wildcard = WildcardTypeName.subtypeOf(klass.className)
    return ParameterizedTypeName.get(rawType, wildcard)
}

fun androidInjectorType(appType: TypeName): ParameterizedTypeName {
    val rawType = AndroidInjector::class.className
    return ParameterizedTypeName.get(rawType, appType)
}

fun androidInjectorBuilderType(appType: TypeName): ParameterizedTypeName {
    val rawType = AndroidInjector.Builder::class.className
    return ParameterizedTypeName.get(rawType, appType)
}
