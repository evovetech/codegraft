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

package sourcerer.inject.descriptor

import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import kotlin.annotation.AnnotationRetention.BINARY

annotation
class GenerateAnnotation(
    val value: Array<out AnnotationTarget> = [],
    val retention: AnnotationRetention = BINARY,
    val documented: Boolean = true
)

interface AnnotationDescriptor<E : Element, D : AnnotationDefinition> {
    val element: E
    val definition: D
}

interface Request {
    val key: Key
}

interface Resolution {
    val key: Key
    val request: Request
}

open
class ModuleDescriptor(
    override val element: TypeElement,
    override val definition: ModuleDefinition
) : AnnotationDescriptor<TypeElement, ModuleDefinition>

interface Descriptor {
}

open
class Declaration(
    val name: String,
    val target: Array<out AnnotationTarget> = arrayOf(),
    val retention: AnnotationRetention = BINARY,
    val documented: Boolean = true,
    val definition: Definition = object : Definition {}
)

interface Definition {

}


/*

class Module1 {
    @Provides

}

 */
