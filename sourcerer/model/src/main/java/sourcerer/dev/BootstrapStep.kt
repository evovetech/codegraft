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

package sourcerer.dev

import sourcerer.AnnotationElements
import sourcerer.AnnotationType
import sourcerer.Env
import sourcerer.Output
import sourcerer.ProcessStep

class BootstrapStep : ProcessStep {
    override
    fun Env.annotations(): Set<AnnotationType> = setOf(
//        Bootstrap::class
    )

    override
    fun supportedOptions(): Iterable<Option> = Option.values()
            .toSet()

    override
    fun Env.process(
        annotationElements: AnnotationElements
    ): Map<AnnotationType, List<Output>> {
        val opt = options
        log("")
        log("options=$opt")
        log("package=${opt[Option.Package]}")
        log("")
        return emptyMap()
    }

    enum
    class Option(
        override val key: String,
        override val defaultValue: String
    ) : sourcerer.processor.Env.Option {
        Package(
            "evovetech.processor.package",
            "evovetech.processor"
        );
    }
}


