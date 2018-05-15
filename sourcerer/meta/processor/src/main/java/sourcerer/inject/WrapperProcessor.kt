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

package sourcerer.inject

import com.google.auto.service.AutoService
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic.Kind

@AutoService(Processor::class)
class WrapperProcessor : AbstractProcessor() {
    override fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(GenerateWrappers::class.java)
                .map(Class<*>::getCanonicalName)
                .toSet()
    }

    override
    fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        roundEnv.getElementsAnnotatedWith(GenerateWrappers::class.java).forEach {
            processingEnv.messager.printMessage(Kind.NOTE, "genwrappers", it)
            val genWrappers = it.getAnnotation(GenerateWrappers::class.java)
            genWrappers?.apply {
                processingEnv.messager.printMessage(Kind.NOTE, "anno=${this}")
                value.forEach {
                    processingEnv.process(it)
                }
            }
        }
        return false
    }

    override
    fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    fun ProcessingEnvironment.process(wrapper: GenerateWrapper) {
        messager.printMessage(Kind.NOTE, "  --anno=$wrapper")

    }
}
