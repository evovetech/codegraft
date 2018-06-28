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

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeSpec
import sourcerer.processor.Env
import javax.lang.model.element.TypeElement
import kotlin.reflect.KClass

typealias SingleStepHandler<A> = Env.(
    input: Input<A>
) -> Output

typealias SingleStepProcessor<A> = AnnotationStep<A>

interface SingleStep<A : Annotation> : FullStep {
    val inputType: KClass<A>

    fun annotationTypes(): Set<KClass<out Annotation>> =
        setOf(inputType)

    override
    fun invoke(
        env: Env,
        input: AnnotationElements
    ): Collection<Output>
}

fun Collection<SingleStep<*>>.annotationTypes() = this
        .flatMap { it.annotationTypes() }

class DefaultSingleStep<A : Annotation>(
    override val inputType: KClass<A>,
    private val handle: SingleStepProcessor<A>
) : SingleStep<A> {
    override
    fun invoke(
        env: Env,
        input: AnnotationElements
    ): Collection<Output> = input.inputs(inputType.java)
            .map { handle(env, it) }
}

inline
fun <reified A : Annotation> singleStep(
    noinline handler: SingleStepHandler<A>
) = DefaultSingleStep(A::class, handler)

fun Env.addGeneratedAnnotation(
    builder: TypeSpec.Builder,
    comments: String = ""
) = builder.apply {
    addAnnotation(Codegen.Inject.Generated) {
        addMember("value", "\$S", processorType.java.canonicalName)
        comments.ifNotEmpty {
            addMember("comments", "\$S", it)
        }
    }
}

fun Env.javaClassOutput(
    rawType: ClassName,
    outExt: String,
    comments: String = "",
    init: TypeSpec.Builder.(BaseElement) -> Unit
) = object : JavaOutput(rawType, outExt) {
    private
    val baseElement: BaseElement = this

    override
    fun typeSpec() = typeSpec {
        init(baseElement)
        addGeneratedAnnotation(this, comments)
    }
}

fun Env.javaClassOutput(
    element: TypeElement,
    outExt: String,
    comments: String = "",
    init: TypeSpec.Builder.(BaseElement) -> Unit
) = javaClassOutput(ClassName.get(element)!!, outExt, comments, init)

fun Env.javaClassOutput(
    input: Input<*>,
    outExt: String,
    comments: String = "",
    init: TypeSpec.Builder.(BaseElement) -> Unit
) = javaClassOutput(input.asTypeElement()!!.element, outExt, comments, init)
