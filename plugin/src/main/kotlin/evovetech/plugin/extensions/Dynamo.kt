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

import groovy.lang.Closure
import groovy.lang.MissingPropertyException
import org.gradle.internal.metaobject.AbstractDynamicObject
import org.gradle.internal.metaobject.DynamicInvokeResult

open
class Dynamo(
    private val parent: DynamoAware
) : AbstractDynamicObject() {
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
        return parent.hasProperty(key)
    }

    override
    fun getProperties(): MutableMap<String, *> {
        return parent.properties
    }

    override
    fun trySetProperty(name: String, value: Any?): DynamicInvokeResult {
        parent.setProperty(name, value)
        return DynamicInvokeResult.found()
    }

    override
    fun tryGetProperty(key: String): DynamicInvokeResult {
        return if (hasProperty(key)) {
            DynamicInvokeResult.found(parent.getProperty(key))
        } else {
            DynamicInvokeResult.notFound()
        }
    }

    override
    fun tryInvokeMethod(name: String, vararg args: Any?): DynamicInvokeResult {
        val result = super.tryInvokeMethod(name, *args)
        if (result.isFound) {
            return result
        }
        val value = this.getProperty(name)
        return if (value is Closure<*>) {
            var closure: Closure<*> = value
            closure = closure.clone() as Closure<*>
            closure.delegate = this
            DynamicInvokeResult.found(closure.call(*args))
        } else {
            DynamicInvokeResult.notFound()
        }
    }

    override
    fun hasMethod(name: String, vararg args: Any?): Boolean {
        return hasProperty(name)// || findMethods(args).isEmpty()
    }
}
