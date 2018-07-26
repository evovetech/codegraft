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

package codegraft.android

import codegraft.inject.AndroidInject
import codegraft.inject.android.AndroidApplication
import codegraft.inject.android.AndroidInjectActivityModule
import codegraft.inject.android.AndroidInjectApplicationModule
import codegraft.inject.android.AndroidInjectBroadcastReceiverModule
import codegraft.inject.android.AndroidInjectContentProviderModule
import codegraft.inject.android.AndroidInjectModule
import codegraft.inject.android.AndroidInjectServiceModule
import codegraft.inject.android.AndroidInjectSupportFragmentModule
import com.google.auto.common.MoreTypes
import com.google.common.base.Equivalence
import com.google.common.collect.ImmutableList
import com.squareup.javapoet.ClassName
import dagger.internal.codegen.getTypeListValue
import sourcerer.getAnnotationMirror
import sourcerer.qualifiedName
import sourcerer.toImmutableList
import javax.inject.Inject
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import kotlin.reflect.KClass

class AndroidInjectModuleDescriptor(
    val element: TypeElement,

    val kind: Kind,

    val annotationMirror: AnnotationMirror,

    val includes: ImmutableList<TypeMirror>
) {
    val type: TypeMirror = element.asType()

    val typeWrapper: Equivalence.Wrapper<TypeMirror> =
        MoreTypes.equivalence().wrap(type)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AndroidInjectModuleDescriptor

        if (typeWrapper != other.typeWrapper) return false

        return true
    }

    override fun hashCode(): Int {
        return typeWrapper.hashCode()
    }

    enum
    class Kind(
        val componentType: KClass<out Any>,
        val moduleType: KClass<out AndroidInjectModule<*>>
    ) {
        Application(
            AndroidApplication::class,
            AndroidInjectApplicationModule::class
        ),
        Activity(
            android.app.Activity::class,
            AndroidInjectActivityModule::class
        ),
        Fragment(
            android.app.Fragment::class,
            AndroidInjectActivityModule::class
        ),
        Service(
            android.app.Service::class,
            AndroidInjectServiceModule::class
        ),
        BroadcastReceiver(
            android.content.BroadcastReceiver::class,
            AndroidInjectBroadcastReceiverModule::class
        ),
        ContentProvider(
            android.content.ContentProvider::class,
            AndroidInjectContentProviderModule::class
        ),
        SupportFragment(
            Any::class,
//            codegraft.inject.SupportFragment::class,
            AndroidInjectSupportFragmentModule::class
        );

        fun getType(elements: Elements): TypeMirror {
            return elements.getTypeElement(componentType.java.canonicalName)
                    .asType()
        }

        class Factory
        @Inject constructor(
            val elements: Elements,
            val types: Types
        ) {
            private
            val allTypes = Kind.values().map {
                it.componentType
            }.toImmutableList()

            fun forElement(
                typeElement: TypeElement
            ): Kind {
                val testType = typeElement.asType()
                values().forEach { kind ->
                    if (types.isSubtype(testType, kind.getType(elements))) {
                        return kind
                    }
                }
                throw IllegalArgumentException("$typeElement does not extend from $allTypes")
            }
        }
    }

    class Factory
    @Inject constructor(
        val elements: Elements,
        val types: Types,
        val kindFactory: codegraft.android.AndroidInjectModuleDescriptor.Kind.Factory
    ) {
        fun forStoredModule(
            className: ClassName
        ): AndroidInjectModuleDescriptor {
            val typeElement = elements.getTypeElement(className.qualifiedName)
            return create(typeElement)
        }

        fun create(element: TypeElement): AndroidInjectModuleDescriptor {
            val kind = kindFactory.forElement(element)
            val annotationMirror = element.getAnnotationMirror<AndroidInject>()!!
            val includes = annotationMirror.getTypeListValue("includes")
            return AndroidInjectModuleDescriptor(
                element = element,
                kind = kind,
                annotationMirror = annotationMirror,
                includes = includes
            )
        }
    }
}
