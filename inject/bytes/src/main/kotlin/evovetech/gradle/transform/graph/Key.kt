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
    fun toString() = type.typeName.toString()

    override
    fun compareTo(other: Key): Int {
        return type.typeName.compareTo(other.type.typeName)
    }
}

data
class Binding(
    val key: Key,
    val kind: Kind,
    val dependencies: Set<BoundEdge>
) {
    val str: String = "${kind.name}Binding<$key>"

    fun children(): Set<BoundEdge> = dependencies
            .flatMap { it.binding.allEdges() }
            .toSet()

    fun allEdges(): Set<BoundEdge> =
        dependencies + children()

    fun topLevel(): Set<BoundEdge> {
        val top = HashSet(dependencies)
        top.removeAll(children())
        return top
    }

    fun hasEdge(other: Binding): Boolean {
        val hasEdge = hasEdgeInternal(other)
        println("$key.hasEdge(${other.key})=$hasEdge")
        return hasEdge
    }

    private
    fun hasEdgeInternal(other: Binding): Boolean {
//        return this == other.binding ||
        val ours = allEdges().map(BoundEdge::binding)
//        if (ours.contains(other)) {
//            return true
//        }
        val theirs = other.dependencies.map(BoundEdge::binding)
        return ours.any(theirs::contains)
    }

    override
    fun toString() = str

    enum class Kind {
        Internal,
        External
    }
}

fun Binding.boundEdge(parent: TypeDefinition, kind: DependencyEdge.Kind): BoundEdge {
    val dep = DependencyEdge(key, parent, kind)
    return BoundEdge(this, dep)
}

data
class BoundEdge(
    val binding: Binding,
    val edge: DependencyEdge
) {
    override
    fun toString(): String {
        return "BoundEdge<${edge.key}>"
    }

    fun hasEdge(other: BoundEdge): Boolean {
        return binding.hasEdge(other.binding)
    }
}

data
class DependencyEdge(
    val key: Key,
    val parent: TypeDefinition,
    val kind: Kind
) {
    override
    fun toString(): String {
        return "from=${parent.typeName}"
    }

    enum
    class Kind {
        SuperClass,
        Interface
    }
}

fun compare1(o1: Binding, o2: Binding): Int {
    if (o2.dependencies
                .map(BoundEdge::binding)
                .contains(o1)) {
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


typealias Net = MutableNetwork<Binding, DependencyEdge>

fun newNetwork(): Net = NetworkBuilder
        .directed()
        .nodeOrder<Binding>(elementOrder)
        .build<Binding, DependencyEdge>()

private val elementOrder: ElementOrder<Binding> = ElementOrder.sorted { o1, o2 ->
    if (o1.key == o2.key) {
        return@sorted 0
    }
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
                .map { it.boundEdge(type, SuperClass) }
                .toSet()
        val interfaces = type.interfaces
                .mapNotNull { resolve(it) }
                .map { it.boundEdge(type, Interface) }
                .toSet()
        val all = supers + interfaces
//        val topLevel = all.flatMap { it.binding. }

        val key = Key(type)
        val kind = if (internals.contains(type)) Internal else External
        val node1 = Binding(key, kind, all)
        addNode(node1)

        node1.dependencies.filter {
            val node2 = it.binding
            val edge = it.edge
            println("")
            val keep = !node1.hasEdge(node2)
            if (!keep) {
                println("    skipping $it")
            }
            println("    node1=${node1.key}${node1.dependencies}")
            println("    node2=${node2.key}${node2.dependencies}")
            println("    edge=$edge")
            keep
        }.forEach {
            val node2 = it.binding
            val edge = it.edge
            try {
                addEdge(node1, node2, edge)
            } catch (e: NullPointerException) {
                println("null pointers")
            }
        }
//        all.forEach {
//            val node2 = it.key
//            val edge = it.value
//            if (!edgesConnecting(node1, node2).contains(edge)) {
//                println("    node1=${node1.key}${node1.dependencies}")
//                println("    node2=${node2.key}${node2.dependencies}")
//                println("    edge=$edge")
//                addEdge(node1, node2, edge)
//            }
//        }
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
