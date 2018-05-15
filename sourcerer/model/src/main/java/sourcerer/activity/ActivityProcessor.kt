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

package sourcerer.activity

import sourcerer.AnnotatedElement
import sourcerer.MultiStep
import sourcerer.MultiStepProcessor
import sourcerer.NoOutput
import sourcerer.inject.LibModule
import sourcerer.lib.LibModuleStep
import sourcerer.processor.Env
import javax.lang.model.element.Element

/**
 * Created by layne on 2/27/18.
 */

open
class MainProcessor(
    isModule: Boolean,
    init: MultiStep.Builder.() -> Unit = {}
) : MultiStepProcessor({
    add(ActivityStep(isModule))
//    addStep(generatedStep<InjectActivity> {
//        log(it.element, "generated InjectActivity")
//        NoOutput
//    })
    add(LibModuleStep(isModule))
    addStep<LibModule> {
        log(it.element, "LibModule")
        NoOutput
    }
    add(ActivityCollectionStep())
//    add(IntoCollectionStep(LibModule::class))
//    add(IntoCollectionStep(Module::class))
//    add(IntoCollectionStep(InjectActivity::class))
//    addStep<IntoCollection> {
//        val value = typeOf(it.annotation::value)
//        log(it.element, "intoCollection -> $value")
//        NoOutput
//    }
    init()
})

inline
fun <reified A : Annotation> Env.log(elem: AnnotatedElement<A, *>, count: Int) {
    log<A>(elem.element, count)
}

inline
fun <reified A : Annotation> Env.log(element: Element, count: Int) {
    log(element, "count=$count, inject=${A::class.java.simpleName}")
}
