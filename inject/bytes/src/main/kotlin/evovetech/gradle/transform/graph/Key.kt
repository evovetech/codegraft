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
import com.google.common.graph.Network
import com.google.common.graph.NetworkBuilder
import evovetech.gradle.transform.graph.Binding.Kind.External
import evovetech.gradle.transform.graph.Binding.Kind.Internal
import evovetech.gradle.transform.graph.DependencyEdge.Kind.Interface
import evovetech.gradle.transform.graph.DependencyEdge.Kind.SuperClass
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
    val kind: Kind,
    val dependencies: List<DependencyEdge>
) {
    constructor(
        key: Key,
        kind: Kind,
        vararg dependencies: DependencyEdge
    ) : this(key, kind, dependencies.toList())

    val str: String = "${kind.name}Binding<$key>"

//    init {
//        val prefix = if (kind == Internal) "Internal" else "External"
//        var _str = "Binding<$key>"
////        if (dependencies.isNotEmpty()) {
////            _str += "{ dependencies=$dependencies) }"
////        }
//        str = _str
//    }

    override
    fun toString() = str

    enum class Kind {
        Internal,
        External
    }
}

data
class DependencyEdge(
    val key: Key,
    val kind: Kind
) {
    override
    fun toString(): String {
        return "Dep<$key>"
    }

    enum
    class Kind {
        SuperClass,
        Interface
    }
}

fun compare1(o1: Binding, o2: Binding): Int {
    if (o2.dependencies
                .map(DependencyEdge::key)
                .contains(o1.key)) {
        return 1
    }
    return 0
}

fun compare(o1: Binding, o2: Binding): Int {
    if (compare1(o1, o2) != 0) {
        return -1
    }
    if (compare1(o2, o1) != 0) {
        return 1
    }
    return 0
}

fun newGraph(): MutableGraph<Binding> = GraphBuilder.directed()
        .nodeOrder<Binding>(elementOrder)
        .build<Binding>()


typealias Net = MutableNetwork<Binding, DependencyEdge>

fun newNetwork(): Net = NetworkBuilder
        .directed()
        .nodeOrder<Binding>(elementOrder)
        .build<Binding, DependencyEdge>()

private val elementOrder: ElementOrder<Binding> = ElementOrder.sorted { o1, o2 ->
    var result = compare(o1, o2)
    if (result == 0) {
        result = o2.dependencies.size - o1.dependencies.size
    }
    if (result == 0) {
        result = o1.key.compareTo(o2.key)
    }
    result
}

class Resolver {
    private val internals = HashSet<TypeDefinition>()

    fun add(type: TypeDefinition) = internals.add(type)

    fun resolve(): Network<Binding, DependencyEdge> {
        val graph = newNetwork()
        graph.resolveAll()
        return graph
    }

    private
    fun Net.resolveAll() {
        internals.forEach { resolve(it) }
    }

    private
    fun Net.resolve(type: TypeDefinition): Binding? {
        if (type.represents(Object::class.java)) {
            return null
        }
        val supers = type.superClass?.toList().orEmpty()
                .mapNotNull { resolve(it) }
                .groupBy { it }
                .mapValues { DependencyEdge(it.key.key, SuperClass) }
        val interfaces = type.interfaces
                .mapNotNull { resolve(it) }
                .groupBy { it }
                .mapValues { DependencyEdge(it.key.key, Interface) }
        val all = supers + interfaces
        val deps = all.values.flatMap { listOf(it) }

        val key = Key(type)
        val kind = if (internals.contains(type)) Internal else External
        val node1 = Binding(key, kind, deps)
        addNode(node1)
        all.forEach {
            val node2 = it.key
            val edge = it.value
            if (!edgesConnecting(node1, node2).contains(edge)) {
                addEdge(node1, node2, edge)
            }
        }
        return node1
    }
}
//
//fun MutableGraph<Binding>.add(type: TypeDefinition): Binding? {
//    if (type.represents(Object::class.java)) {
//        return null
//    }
//    val bindings = ArrayList<Binding>()
//    val deps = ArrayList<Dependency>()
//    type.superClass
//            ?.let { add(it) }
//            ?.also { bindings.add(it) }
//            ?.let { Dependency(it.key, SuperClass) }
//            ?.run(deps::add)
//    type.interfaces
//            .mapNotNull { add(it) }
//            .onEach { bindings.add(it) }
//            .map { Dependency(it.key, Interface) }
//            .map(deps::add)
//
//    val key = Key(type)
//    val node = Binding(key, deps.toList())
//    fun addEdge(target: Binding): Boolean =
//        putEdge(node, target)
//
//    addNode(node)
//    bindings.map(::addEdge)
//    return node
//}
