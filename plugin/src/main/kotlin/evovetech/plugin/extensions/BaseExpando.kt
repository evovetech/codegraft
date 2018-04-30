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

package evovetech.plugin.extensions

import groovy.lang.MissingPropertyException
import groovy.util.Expando
import org.gradle.api.internal.DynamicObjectAware
import org.gradle.internal.metaobject.AbstractDynamicObject
import org.gradle.internal.metaobject.DynamicInvokeResult
import org.gradle.internal.metaobject.DynamicObject
import kotlin.reflect.KProperty

open
class BaseExpando : Expando(), DynamicObjectAware {
    val dynamo by lazy {
        println("${this::class.java}.asDynamicObject()")
        Dynamo()
    }

    override
    fun getAsDynamicObject(): DynamicObject {
        return dynamo
    }

//    fun findProperty(name: String) = this::class.memberProperties
//            .firstOrNull { it.name == name }
//
//    fun findMethods(name: String) = this::class.memberFunctions
//            .filter { it.name == name }

    override
    fun getProperty(name: String): Any? {
//        findProperty(name)?.let {
//            return it.call(this)
//        }
        return super.getProperty(name)
    }

    fun invokeMethod(name: String, vararg args: Any?): Any? {
//        findMethods(name).firstOrNull {
//            try {
//                return it.call(this, *args)
//            } catch (e: Throwable) {
//                false
//            }
//        }
        return super.invokeMethod(name, args)
    }

    fun <T> property(initializer: ((thisRef: Any?) -> T)? = null) =
        LazyProperty(initializer)

    inner
    class LazyProperty<T>(
        val initializer: ((thisRef: Any?) -> T)? = null
    ) {
        operator
        fun <T> getValue(thisRef: Any?, property: KProperty<*>): T {
            val key = property.name
            val p = properties[key] ?: initializer?.invoke(thisRef)?.apply {
                setProperty(key, this)
            }
            @Suppress("UNCHECKED_CAST")
            return (p ?: getProperty(key)) as T
        }

        operator
        fun <T> setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            setProperty(property.name, value)
        }
    }

    inner
    class Dynamo : AbstractDynamicObject() {
        val parent = this@BaseExpando

        override
        fun getProperty(name: String): Any {
            return super.getProperty(name)
        }

        override
        fun invokeMethod(name: String, vararg arguments: Any?): Any {
            return super.invokeMethod(name, *arguments)
        }

        override
        fun getMissingProperty(name: String): MissingPropertyException {
            println("properties=$properties")
            return super.getMissingProperty(name)
        }

        override
        fun getDisplayName(): String {
            return publicType.canonicalName
        }

        override
        fun getPublicType(): Class<*> {
            return parent::class.java
        }

        override
        fun hasProperty(key: String): Boolean {
//            parent.findProperty(key)?.let {
//                return true
//            }
            return parent.properties.containsKey(key)
        }

        override
        fun getProperties(): MutableMap<String, *> {
            return parent.properties.mapKeys {
                it.key.toString()
            }.toMutableMap()
        }

        override
        fun trySetProperty(name: String, p1: Any?): DynamicInvokeResult {
//            findProperty(name)?.let {
//                val result = it.call(parent)
//                return DynamicInvokeResult.found(result)
//            }
            parent.properties[name] = p1
            return DynamicInvokeResult.found()
        }

        override
        fun tryGetProperty(key: String): DynamicInvokeResult {
            return if (hasProperty(key)) {
                DynamicInvokeResult.found(parent.properties[key])
            } else {
                DynamicInvokeResult.notFound()
            }
        }

        override
        fun tryInvokeMethod(name: String, vararg args: Any?): DynamicInvokeResult = try {
            val result = parent.invokeMethod(name, *args)
            DynamicInvokeResult.found(result)
        } catch (e: Throwable) {
            DynamicInvokeResult.notFound()
        }

        override
        fun hasMethod(p0: String, vararg p1: Any?): Boolean {
            return hasProperty(p0)// || findMethods(p0).isEmpty()
        }
    }
}
