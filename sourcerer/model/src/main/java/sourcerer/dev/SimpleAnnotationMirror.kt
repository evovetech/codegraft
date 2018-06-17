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

package sourcerer.dev

import com.google.auto.common.MoreTypes
import com.google.common.base.Joiner
import com.google.common.base.Preconditions.checkArgument
import com.google.common.collect.FluentIterable
import com.google.common.collect.ImmutableMap
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.util.ElementFilter.methodsIn

/** A representation of an annotation.  */
internal class SimpleAnnotationMirror
private constructor(
    private val annotationType: TypeElement,
    namedValues: Map<String, AnnotationValue>
) : AnnotationMirror {
    private val namedValues: ImmutableMap<String, out AnnotationValue>
    private val elementValues: ImmutableMap<ExecutableElement, out AnnotationValue>

    init {
        checkArgument(
            annotationType.kind == ElementKind.ANNOTATION_TYPE,
            "annotationType must be an annotation: %s",
            annotationType
        )
        checkArgument(
            FluentIterable.from<ExecutableElement>(methodsIn(annotationType.enclosedElements))
                    .transform { element -> element?.simpleName.toString() }
                    .toSet() == namedValues.keys,
            "namedValues must have values for exactly the members in %s: %s",
            annotationType,
            namedValues
        )
        this.namedValues = namedValues.toImmutableMap()
        this.elementValues = methodsIn(annotationType.enclosedElements)
                .groupBy { it }
                .mapValues { namedValues[it.key.simpleName.toString()] }
                .filterValues { it != null }
                .mapValues { it.value!! }
                .toImmutableMap()
    }

    override fun getAnnotationType(): DeclaredType {
        return MoreTypes.asDeclared(annotationType.asType())
    }

    override fun getElementValues(): Map<ExecutableElement, AnnotationValue> {
        return elementValues
    }

    override fun toString(): String {
        val builder = StringBuilder("@").append(annotationType.qualifiedName)
        if (!namedValues.isEmpty()) {
            builder
                    .append('(')
                    .append(Joiner.on(", ").withKeyValueSeparator(" = ").join(namedValues))
                    .append(')')
        }
        return builder.toString()
    }

    companion object {

        /**
         * An object representing an annotation instance.
         *
         * @param annotationType must be an annotation type
         * @param namedValues a value for every annotation member, including those with defaults, indexed
         * by simple name
         */
        @JvmStatic @JvmOverloads
        fun of(
            annotationType: TypeElement,
            namedValues: Map<String, AnnotationValue> = ImmutableMap.of()
        ): AnnotationMirror {
            return SimpleAnnotationMirror(annotationType, namedValues)
        }
    }
}
