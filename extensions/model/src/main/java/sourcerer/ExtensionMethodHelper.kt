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

package sourcerer

import com.google.common.collect.ImmutableList
import java.util.ArrayList
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

internal class ExtensionMethodHelper private constructor(
    val kind: ExtensionMethod.Kind,
    val method: ExecutableElement,
    returnAnnotations: List<AnnotationMirror>
) {
    val returnAnnotations: ImmutableList<AnnotationMirror>

    init {
        if (kind == ExtensionMethod.Kind.Instance && method.parameters.size > 0) {
            throw IllegalArgumentException("instance method cannot have parameters")
        }
        this.returnAnnotations = ImmutableList.copyOf(returnAnnotations)
    }

    fun name(): String {
        return method.simpleName.toString()
    }

    companion object {
        val ANNOTATION_TYPE: Class<ExtensionMethod> = ExtensionMethod::class.java
        val ANNOTATION_NAME = ANNOTATION_TYPE.canonicalName

        fun process(memberElement: Element): ExtensionMethodHelper? {
            var methodKind: ExtensionMethod.Kind? = null
            val others = ArrayList<AnnotationMirror>()
            for (am in memberElement.annotationMirrors) {
                val kind = parseAnnotation(memberElement, am, others)
                if (methodKind != null && kind != null) {
                    val format = "Cannot have annotation '%s' when it is already present"
                    val message = String.format(format, ANNOTATION_TYPE)
                    throw IllegalStateException(message)
                } else if (kind != null) {
                    validate(kind, memberElement)
                    methodKind = kind
                }
            }
            return if (methodKind == null)
                null
            else
                ExtensionMethodHelper(methodKind, memberElement as ExecutableElement, others)
        }

        private fun parseAnnotation(
            memberElement: Element,
            am: AnnotationMirror,
            others: MutableList<AnnotationMirror>
        ): ExtensionMethod.Kind? {
            val te = am.annotationType.asElement() as TypeElement
            val name = te.qualifiedName.toString()
            if (ANNOTATION_NAME == name) {
                val method = memberElement.getAnnotation(ANNOTATION_TYPE)
                return method.value
            }
            others.add(am)
            return null
        }

        private fun validate(
            kind: ExtensionMethod.Kind,
            element: Element
        ) {
            val name = element.simpleName.toString()
            val format = String.format("'%s' element with method kind '%s' %s", name, kind, "%s")
            if (element.kind != ElementKind.METHOD) {
                val message = String.format(format, "must be a method")
                throw IllegalArgumentException(message)
            }
            val modifiers = element.modifiers
            if (!modifiers.contains(Modifier.PUBLIC)) {
                val message = String.format(format, "must be public")
                throw IllegalArgumentException(message)
            }
            if (kind == ExtensionMethod.Kind.Instance) {
                if (!modifiers.contains(Modifier.STATIC)) {
                    val message = "Instance Method " + String.format(format, "must be static")
                    throw IllegalArgumentException(message)
                }
            } else if (modifiers.contains(Modifier.STATIC)) {
                val message = String.format(format, "must not be static")
                throw IllegalArgumentException(message)
            }
        }
    }
}
