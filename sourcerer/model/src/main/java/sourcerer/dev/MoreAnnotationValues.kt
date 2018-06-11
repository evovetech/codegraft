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

import com.google.common.collect.ImmutableList
import javax.lang.model.element.AnnotationValue
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.SimpleAnnotationValueVisitor8

/** Utility methods for working with [AnnotationValue] instances.  */
internal object MoreAnnotationValues {
    private val AS_ANNOTATION_VALUES =
        object : SimpleAnnotationValueVisitor8<ImmutableList<AnnotationValue>, String?>() {
            override fun visitArray(
                vals: List<AnnotationValue>, elementName: String?
            ): ImmutableList<AnnotationValue> {
                return ImmutableList.copyOf(vals)
            }

            override fun defaultAction(o: Any?, elementName: String?): ImmutableList<AnnotationValue> {
                throw IllegalArgumentException("$elementName is not an array: $o")
            }
        }

    private val AS_TYPE = object : SimpleAnnotationValueVisitor8<TypeMirror, Void?>() {
        override fun visitType(t: TypeMirror, p: Void?): TypeMirror {
            return t
        }

        override fun defaultAction(o: Any, p: Void?): TypeMirror {
            throw TypeNotPresentException(o.toString(), null)
        }
    }

    /**
     * Returns the list of values represented by an array annotation value.
     *
     * @throws IllegalArgumentException unless `annotationValue` represents an array
     */
    fun asAnnotationValues(annotationValue: AnnotationValue): ImmutableList<AnnotationValue> {
        return annotationValue.accept(AS_ANNOTATION_VALUES, null)
    }

    /**
     * Returns the type represented by an annotation value.
     *
     * @throws IllegalArgumentException unless `annotationValue` represents a single type
     */
    fun asType(annotationValue: AnnotationValue): TypeMirror {
        return AS_TYPE.visit(annotationValue)
    }
}
