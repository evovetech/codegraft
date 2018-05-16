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

import sourcerer.inject.descriptor.ElementDeclaration.Group
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import kotlin.annotation.AnnotationRetention.BINARY
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.TYPEALIAS
import kotlin.reflect.KClass

interface AnnotationDefinition

interface AbstractComponentDefinition : AnnotationDefinition {
    val modules: Array<out ModuleDefinition>

    @AnnotationDefinition2(
        name = "Builder",
        target = [CLASS]
    )
    interface BuilderDefinition : AnnotationDefinition
}

@AnnotationDefinition2(
    name = "Component",
    target = [CLASS]
)
interface ComponentDefinition : AbstractComponentDefinition {
    val dependencies: Array<out ComponentDefinition>
}

@AnnotationDefinition2(
    name = "Subcomponent",
    target = [CLASS]
)
interface SubcomponentDefinition : AnnotationDefinition {
    val modules: Array<out ModuleDefinition>
}

@AnnotationDefinition2(
    name = "Module",
    target = [CLASS]
)
interface ModuleDefinition : AnnotationDefinition {
    val includes: Array<out ModuleDefinition>
    val subcomponents: Array<out SubcomponentDefinition>
}

annotation
class Cd(
    val modules: Array<out Md> = [],
    val dependencies: Array<out Cd> = []
)

annotation
class Md(
    val includes: Array<out Md> = [],
    val subcomponents: Array<out Sd> = []
)

annotation
class Sd(
    val modules: Array<out Md> = []
)

@AnnotationDefinition2(
    name = "Binds",
    target = [FUNCTION]
)
interface BindsDefinition : AnnotationDefinition

@AnnotationDefinition2(
    name = "Provides",
    target = [FUNCTION]
)
interface ProvidesDefinition : AnnotationDefinition

interface ElementDeclaration {
    interface Group<out E : ElementDeclaration> : Iterable<E>
}

typealias IO<Input, Output> = (Input) -> Output

interface ProvidesDeclaration : ElementDeclaration {
    @AnnotatedWith(ProvidesDefinition::class)
    fun provides(vararg dependencies: Dependency): Dependency
}

interface BindsDeclaration : ElementDeclaration {
    @AnnotatedWith(BindsDefinition::class)
    fun binds(dependency: Dependency): Dependency
}

interface ModuleDeclaration : ElementDeclaration {
    val binds: Group<BindsDeclaration>
    val provides: Group<ProvidesDeclaration>
}

interface Dependency {
    val key: Key
}

/*

AnnotationDefinition: Describes the annotation to be generated
AnnotationDeclaration: Describes an instance of the annotation

ElementDefinition: Describes the

@Component(
    modules=[]
)
interface AppComponent {
    val
}
*/

@Target(
    CLASS,
    TYPEALIAS,
    FUNCTION
)
annotation
class AnnotatedWith(
    val value: KClass<out AnnotationDefinition>
)

@Target(CLASS)
annotation
class AnnotationDefinition2(
    val name: String,
    val target: Array<out AnnotationTarget> = [],
    val retention: AnnotationRetention = BINARY,
    val documented: Boolean = true
)
