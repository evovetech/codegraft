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

import com.squareup.javapoet.ClassName
import sourcerer.BaseElement
import sourcerer.Dagger
import sourcerer.addAnnotation
import sourcerer.addTo
import sourcerer.typeSpec
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PUBLIC

/**
 * Created by layne on 2/25/18.
 */
open
class LibModuleElement(
    final override val rawType: ClassName,
    includes: Collection<ClassName> = emptySet()
) : BaseElement,
    MutableSet<ClassName> by HashSet(includes) {
    final override val outExt = "LibModule"

    override fun typeSpec() = typeSpec {
        addModifiers(PUBLIC, FINAL)
        addAnnotation(Dagger.Module) {
            forEach(addTo("includes"))
        }
    }
}
