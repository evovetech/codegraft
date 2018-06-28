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

package sourcerer.lib

import sourcerer.processor.BaseProcessingEnv
import sourcerer.processor.Env

/**
 * Created by layne on 2/21/18.
 */

class LibEnv(env: Env) : BaseProcessingEnv(env) {
    val modules: LibModuleEnv = LibModuleEnv(env)
    val components: LibComponentEnv = LibComponentEnv(modules)
}
