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

import org.gradle.api.internal.DynamicObjectAware

interface DynamoAware : DynamicObjectAware {
    val properties: ExtMap
    val dynamo: Dynamo

    override
    fun getAsDynamicObject(): Dynamo {
        println("${this::class.simpleName}.getAsDynamicObject()")
        return dynamo
    }
}

fun DynamoAware.hasProperty(name: String): Boolean {
    return properties.containsKey(name)
}

fun DynamoAware.getProperty(name: String): Any? {
    return properties[name]
}

fun DynamoAware.setProperty(name: String, value: Any?) {
    properties[name] = value
}
