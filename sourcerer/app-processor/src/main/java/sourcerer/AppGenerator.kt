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

import sourcerer.lib.LibEnvProcessor
import sourcerer.processor.ProcessingEnv
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement

/**
 * Created by layne on 2/20/18.
 */
//@AutoService(Processor::class)
class AppGenerator : LibEnvProcessor() {
    private var done: Boolean = false

    override
    fun getSupportedOptions(): Set<String> {
        return setOf(Option.Package.key)
    }

    override
    fun process(set: MutableSet<out TypeElement>, roundEnv: RoundEnvironment) = withEnv {
        if (!done) {
            done = true
            val pkg = DefaultPackage(options[Option.Package])
            AppComponentWriter(pkg, modules, components).apply {
                writeTo(filer())
            }
        }
        false
    }

    override
    fun getSupportedAnnotationTypes(): Set<String> {
        return ALL_ANNOTATION_TYPES
    }

    enum
    class Option(
        override val key: String,
        override val defaultValue: String
    ) : ProcessingEnv.Option {
        Package(
            "evovetech.processor.package",
            "evovetech.processor"
        );
    }
}

