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
import dagger.model.BindingGraph
import dagger.model.BindingGraph.DependencyEdge
import dagger.model.BindingGraph.Node
import dagger.spi.BindingGraphPlugin
import dagger.spi.ValidationItem
import javax.annotation.processing.Filer
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic.Kind.NOTE

@AutoService(BindingGraphPlugin::class)
class Grapher : BindingGraphPlugin {
    private lateinit var filer: Filer
    private lateinit var elements: Elements
    private lateinit var types: Types

    override
    fun visitGraph(bindingGraph: BindingGraph): List<ValidationItem> {
        val items = ArrayList<ValidationItem>()
        val root = bindingGraph.rootComponentNode()
        items.add(root.let(node("root node")))

//        bindingGraph.inEdges(root)
//                .filterIsInstance<DependencyEdge>()
//                .map(dep("root -- in edge"))
//                .map(items::add)
//        bindingGraph.outEdges(root)
//                .filterIsInstance<DependencyEdge>()
//                .map(dep("root -- out edge"))
//                .map(items::add)
        bindingGraph.entryPointEdges()
                .map(dep("entry point edge"))
                .map(items::add)
        return items
    }

    fun node(msg: String): (Node) -> ValidationItem = {
        ValidationItem.create(NOTE, it, msg)
    }

    fun dep(msg: String): (DependencyEdge) -> ValidationItem = {
        ValidationItem.create(NOTE, it, msg)
    }

    override
    fun initFiler(filer: Filer) {
        this.filer = filer
    }

    override
    fun initElements(elements: Elements) {
        this.elements = elements
    }

    override
    fun initTypes(types: Types) {
        this.types = types
    }
}
