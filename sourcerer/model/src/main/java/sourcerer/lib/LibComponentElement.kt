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
import sourcerer.interfaceBuilder
import sourcerer.typeSpec
import javax.lang.model.element.Modifier.PUBLIC

/**
 * Created by layne on 2/25/18.
 */
open
class LibComponentElement(
    final override val rawType: ClassName,
    modules: Collection<ClassName> = emptySet(),
    dependencies: Collection<ClassName> = emptySet()
) : BaseElement {
    final override val outExt = "LibComponent"
    val modules: MutableSet<ClassName> = HashSet(modules)
    val dependencies: MutableSet<ClassName> = HashSet(dependencies)

    override fun newBuilder() = outKlass.interfaceBuilder()

    override fun typeSpec() = typeSpec {
        addModifiers(PUBLIC)
        addSuperinterfaces(dependencies.toList())
    }

    fun libModule() = LibModuleElement(rawType, modules)
}
