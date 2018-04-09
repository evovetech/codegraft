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

import com.google.auto.service.AutoService
import sourcerer.processor.BaseProcessor
import java.io.IOException
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement

@AutoService(Processor::class)
class ExtensionProcessor : BaseProcessor() {
    private val processor = Extensions.processor()

    override fun process(
        annotations: Set<TypeElement>,
        env: RoundEnvironment
    ): Boolean {
        var processed: Boolean? = null
        var write = false
        for (annotationElement in annotations) {
            val extensionClass = annotationElement.getAnnotation(ExtensionClass::class.java)
            if (extensionClass == null) {
                processed = false
                continue
            } else if (processed == null) {
                processed = true
            }

            val extension = processor.add(extensionClass)
            for (typeElement in env.getElementsAnnotatedWith(annotationElement)) {
                // Ensure it is a class element
                if (typeElement.kind != ElementKind.CLASS) {
                    error(typeElement, "Only classes can be annotated with @%s", annotationElement.simpleName)
                    return true // Exit processing
                }
                write = write or extension.process(typeElement as TypeElement)
            }
        }

        if (write) {
            try {
                processor.writeTo(processingEnv.filer)
            } catch (e: IOException) {
                error("error processing %s", processor.extensions())
                return true // Exit processing
            }

        }
        return if (processed == null) false else processed
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        // We need to process all annotation types
        return ALL_ANNOTATION_TYPES
    }
}
