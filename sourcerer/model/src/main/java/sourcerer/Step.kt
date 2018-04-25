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
import sourcerer.io.Reader
import kotlin.reflect.KClass

/**
 * Created by layne on 3/19/18.
 */

typealias IoStep<Input, Output> = (env: Env, input: Input) -> Output

typealias AnnotationStep<A> = IoStep<Input<A>, Output>

typealias IntermediateStep = IoStep<Collection<Output>, Output>

typealias FullStep = IoStep<AnnotationElements, Collection<Output>>

object DefaultIntermediateStep : IntermediateStep {
    override
    fun invoke(env: Env, input: Collection<Output>) = NoOutput
}

sealed
class Step

interface Interceptor<Input, Output> {
    fun intercept(chain: Chain<Input, Output>): Output

    interface Chain<Input, out Output> {
        val env: Env
        val value: Input

        fun proceed(input: Input): Output
    }
}

val KClass<*>.className: ClassName
    get() = ClassName.get(java)

//fun Collection<Intermediate<*>>

class StoredFile(
    private val contents: ByteArray
) {
    fun <T> read(func: (Reader) -> T) = Reader.newReader(contents.inputStream())
            .use(func)
}

//abstract class Steps {
//    private abstract
//    val map: Map<KClass<out Annotation>, Collection<IoStep<*, Output>>>
//
//    class Builder : ArrayList<IoStep<*, out Output>>() {
//        inline
//        fun <reified A : Annotation> addAnnotionStep(
//                step: AnnotationStep<A>
//        ) = add(singleFullStep(step))
//    }
//}

//abstract
//class FullStep :
//    IoStep<AnnotationElements, Collection<Output>>()

open
class SingleFullStep<A : Annotation>(
    val type: KClass<A>,
    val annotationStep: AnnotationStep<A>,
    val intermediateStep: IntermediateStep = DefaultIntermediateStep
) : FullStep {
    override
    fun invoke(env: Env, input: AnnotationElements): Collection<Output> {
        val outputs = input.inputs(type.java).map {
            annotationStep(env, it)
        }
        return outputs + intermediateStep(env, outputs)
    }
}

inline
fun <reified A : Annotation> singleFullStep(
    noinline annotationStep: AnnotationStep<A>,
    noinline intermediateStep: IntermediateStep = DefaultIntermediateStep
) = SingleFullStep(A::class, annotationStep, intermediateStep)
