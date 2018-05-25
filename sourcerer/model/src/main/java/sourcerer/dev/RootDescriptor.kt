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

import com.google.auto.service.AutoService
import sourcerer.AnnotationElements
import sourcerer.AnnotationType
import sourcerer.BaseProcessor
import sourcerer.Output
import sourcerer.ProcessStep
import sourcerer.inject.LibComponent
import javax.annotation.processing.Processor

@AutoService(Processor::class)
class RootProcessor : BaseProcessor() {
    override
    fun processSteps(options: Options): List<ProcessStep> {
        return listOf(Step(options))
    }

    override
    fun supportedOptions(): Iterable<Option> = Option.values()
            .toSet()

    class Step(
        private val opt: Options
    ) : ProcessStep {
        override
        fun sourcerer.Env.annotations(): Set<AnnotationType> {
            return setOf(LibComponent::class)
        }

        override
        fun sourcerer.Env.process(
            annotationElements: AnnotationElements
        ): Map<AnnotationType, List<Output>> {
            log("package=${opt[Option.Package]}")
            return emptyMap()
        }
    }

    enum
    class Option(
        override val key: String,
        override val defaultValue: String
    ) : BaseProcessor.Option {
        Package(
            "evovetech.processor.package",
            "evovetech.processor"
        );
    }
}


