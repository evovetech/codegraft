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

import com.google.auto.common.AnnotationMirrors
import com.google.auto.common.MoreElements.isAnnotationPresent
import com.google.auto.common.MoreTypes
import com.google.auto.common.MoreTypes.asTypeElement
import com.google.common.base.Equivalence
import com.google.common.collect.FluentIterable
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import dagger.internal.codegen.qualifier
import javax.inject.Inject
import javax.inject.Qualifier
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.type.PrimitiveType
import javax.lang.model.type.TypeMirror

/**
 * A value object for types and qualifiers.
 *
 * @author Gregory Kick
 */
data
class Key(
    private val wrappedType: Equivalence.Wrapper<TypeMirror>,
    private val wrappedQualifier: Equivalence.Wrapper<AnnotationMirror>? = null
) {
    constructor(
        type: TypeMirror,
        qualifier: AnnotationMirror? = null
    ) : this(
        wrappedType = MoreTypes.equivalence().wrap(type),
        wrappedQualifier = qualifier?.run(AnnotationMirrors.equivalence()::wrap)
    )

    val type: TypeMirror
        get() = wrappedType.get()!!

    val qualifier: AnnotationMirror?
        get() = wrappedQualifier?.get()

    // TODO:
    override
    fun toString(): String {
        val typeQualifiedName = asTypeElement(type).toString()
        return qualifier?.let {
            it.toString() + "/" + typeQualifiedName
        } ?: typeQualifiedName
    }

    internal
    class Factory
    @Inject constructor(
        val types: SourcererTypes
    ) {
        fun create(
            element: Element
        ): Key {
            val type = element.asType()
            val annotations = element.annotationMirrors + type.annotationMirrors
            return create(type, annotations)
        }

        fun create(
            type: TypeMirror,
            annotations: Iterable<AnnotationMirror>
        ): Key {
            val qualifiers = immutableSet<AnnotationMirror> {
                for (annotation in annotations) {
                    if (isAnnotationPresent(annotation.annotationType.asElement(), Qualifier::class.java)) {
                        add(annotation)
                    }
                }
            }
            // TODO(gak): check for only one qualifier rather than using the first
            val qualifier = FluentIterable.from(qualifiers)
                    .first()
                    .orNull()
            val keyType = types.boxedType(type)
            return Key(keyType, qualifier)
        }

        fun forQualifiedType(
            type: TypeMirror,
            qualifier: AnnotationMirror? = null
        ) = Key(type.boxed(), qualifier)

        fun forMembersInjectedType(
            type: TypeMirror
        ) = Key(type)

        fun forMethod(
            method: ExecutableElement,
            keyType: TypeMirror
        ): Key = forQualifiedType(keyType, method.qualifier)

        private
        fun TypeMirror.boxed(): TypeMirror = if (this.kind.isPrimitive) {
            types.boxedClass(this as PrimitiveType).asType()
        } else {
            this
        }
    }
}

val Key.typeName: TypeName
    get() = TypeName.get(type)
val Key.element: Element
    get() = MoreTypes.asElement(type)
val Key.name: String
    get() = element.simpleName.toString()
val Key.fieldName: String
    get() = name.decapitalize()
val Key.getterMethodName: String
    get() = "get${name.capitalize()}"

fun Key.getterMethod(init: MethodSpec.Builder.() -> Unit) = MethodBuilder(getterMethodName) {
    init()
    returns(typeName)
}
