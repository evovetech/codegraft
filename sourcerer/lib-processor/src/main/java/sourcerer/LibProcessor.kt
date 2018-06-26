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
import javax.annotation.processing.Processor

/**
 * Created by layne on 2/20/18.
 */
//@AutoService(Processor::class)
//class LibProcessor : LibEnvProcessor() {
//    override fun getSupportedAnnotationTypes(): Set<String> {
//        return ImmutableSet.builder<String>()
//                .add<LibModule>()
//                .add<LibComponent>()
//                .build()
//    }
//
//    override fun process(set: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean = withEnv {
//        roundEnv.elementsAnnotatedWith<LibComponent>()
//                .takeUnless { it.isEmpty() }
//                ?.also { elements ->
//                    components.readAnnotations(elements)
//                    components.writeSourcererFiles()
//                }
//        roundEnv.elementsAnnotatedWith<LibModule>()
//                .takeUnless { it.isEmpty() }
//                ?.also { elements ->
//                    modules.readAnnotations(elements)
//                    modules.writeSourcererFiles()
//                }
//        false
//    }
//}

@AutoService(Processor::class)
class LibProcessor : MainProcessor(false)
