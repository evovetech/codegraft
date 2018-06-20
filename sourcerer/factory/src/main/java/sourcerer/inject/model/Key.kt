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

package sourcerer.inject.model

import com.google.auto.common.AnnotationMirrors
import com.google.auto.common.MoreElements.isAnnotationPresent
import com.google.auto.common.MoreTypes
import com.google.auto.common.MoreTypes.asTypeElement
import com.google.common.base.Equivalence
import com.google.common.collect.FluentIterable
import javax.inject.Inject
import javax.inject.Qualifier
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
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
    }
}