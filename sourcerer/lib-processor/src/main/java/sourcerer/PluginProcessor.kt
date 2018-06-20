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

import sourcerer.inject.PluginType
import sourcerer.lib.LibEnvProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement

/**
 * Created by layne on 2/21/18.
 */
//@AutoService(Processor::class)
class PluginProcessor : LibEnvProcessor() {
    override
    fun process(set: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        set.typesAnnotatedWith<PluginType>()
                .map(env()::newPluginElement)
                .forEach(logOne("implementation"))
        roundEnv.typesAnnotatedWith<PluginType>()
                .map(env()::newPluginElement)
                .forEach(logOne("declaration"))
        return false
    }

    override
    fun getSupportedAnnotationTypes(): Set<String> {
        return ALL_ANNOTATION_TYPES
    }
}
