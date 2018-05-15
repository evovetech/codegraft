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

import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import kotlin.reflect.KClass

/**
 * Created by layne on 3/19/18.
 */
abstract
class Env : sourcerer.processor.Env {
    abstract
    val processorType: KClass<out Processor>

    constructor(env: Env) : super(env)
    constructor(processingEnv: ProcessingEnvironment) : super(processingEnv)
}
