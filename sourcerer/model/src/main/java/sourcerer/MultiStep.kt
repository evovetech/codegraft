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

import sourcerer.processor.Env

class MultiStep(
    init: Builder.() -> Unit
) : ProcessStep {
    private
    val steps: Map<AnnotationType, List<SingleStep<*>>>

    init {
        val initSteps = Builder()
        initSteps.init()
        steps = initSteps.groupBy { it.inputType }
    }

    override
    fun Env.annotations() = steps
            .flatMap { it.value.annotationTypes() + it.key }
            .toSet()

    override
    fun Env.process(
        annotationElements: AnnotationElements
    ): Map<AnnotationType, List<Output>> {
        val env = this@process
        val outputs = LinkedHashMap<AnnotationType, MutableList<Output>>()
        steps.forEach {
            val cur = outputs.getOrPut(it.key) { ArrayList() }
            it.value.flatMapTo(cur) {
                it(env, annotationElements)
            }
        }
        return outputs
    }

    class Builder : ArrayList<SingleStep<*>>() {
        inline
        fun <reified A : Annotation> addStep(
            noinline handler: SingleStepHandler<A>
        ) = add(singleStep(handler))
    }
}
