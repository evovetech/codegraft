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

package evovetech.gradle.transform.graph

import com.google.common.graph.ElementOrder
import com.google.common.graph.GraphBuilder
import com.google.common.graph.MutableGraph
import com.google.common.graph.MutableNetwork
import com.google.common.graph.NetworkBuilder
import evovetech.gradle.transform.graph.Dependency.Kind.Interface
import evovetech.gradle.transform.graph.Dependency.Kind.SuperClass
import net.bytebuddy.description.type.TypeDefinition

data
class Key(
    val type: TypeDefinition
) : Comparable<Key> {
    override
    fun toString() = type.toString()

    override
    fun compareTo(other: Key): Int {
        return type.typeName.compareTo(other.type.typeName)
    }
}

data
class Binding(
    val key: Key,
    val dependencies: List<Dependency>
) {
    constructor(
        key: Key,
        vararg dependencies: Dependency
    ) : this(key, dependencies.toList())

    val str: String

    init {
        var _str = "Binding<$key>"
        if (dependencies.isNotEmpty()) {
            _str += "{ dependencies=$dependencies) }"
        }
        str = "$_str\n"
    }

    override
    fun toString() = str
}

data
class Dependency(
    val key: Key,
    val kind: Kind = SuperClass
) {
    enum class Kind {
        SuperClass,
        Interface
    }

    override
    fun toString(): String {
        return "Dep<$key>"
    }

}

fun network(): MutableNetwork<Key, Binding> = NetworkBuilder
        .directed()
        .build<Key, Binding>()

fun graph(): MutableGraph<Binding> = GraphBuilder.directed()
        .nodeOrder<Binding>(ElementOrder.sorted { o1, o2 ->
            var result = o2.dependencies.size - o1.dependencies.size
            if (result == 0) {
                result = o1.key.compareTo(o2.key)
            }
            result
        })
        .build<Binding>()

fun MutableGraph<Binding>.add(type: TypeDefinition): Binding? {
    if (type.represents(Object::class.java)) {
        return null
    }
    val deps = ArrayList<Dependency>()
    type.superClass
            ?.let { add(it) }
            ?.let { Dependency(it.key, SuperClass) }
            ?.run(deps::add)
    type.interfaces
            .mapNotNull { add(it) }
            .map { Dependency(it.key, Interface) }
            .map(deps::add)

    val key = Key(type)
    val node = Binding(key, deps.toList())
    addNode(node)
    return node
}
