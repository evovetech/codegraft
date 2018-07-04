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

package sourcerer.bootstrap

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import com.google.common.collect.ImmutableSetMultimap
import sourcerer.AnnotationElements
import sourcerer.Output
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppComponentProcessor
@Inject internal
constructor(
    val appComponentStep: AppComponentStep,
    val sourcerer: BootstrapSourcerer
) {

//    fun Env.postProcess(): List<Output> {
//        if (!processed) {
//            return emptyList()
//        }
//
//        val generatedComponents = this@BootstrapComponentStep.generatedComponents
//                .toImmutableSet()
//        val storedComponents = sourcerer.storedOutputs()
//                .map(componentFactory::forStoredComponent)
//                .toImmutableSet()
//        log("storedComponents = $storedComponents")
//        val appComponent = appComponentStep.process(generatedComponents, storedComponents)
//        return appComponent.flatMap(AppComponentStep.Output::outputs)
//    }
}
